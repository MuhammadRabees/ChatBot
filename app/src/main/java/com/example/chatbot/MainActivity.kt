package com.example.chatbot

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.chatbot.databinding.ActivityMainBinding
import com.example.chatbot.network.AppDatabase
import com.example.chatbot.ChatRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var historyAdapter: ChatHistoryAdapter // Side panel ke liye
    private lateinit var viewModel: ChatViewModel
    private var lastClickTime = 0L

    private var tempCameraUri: Uri? = null
    private var attachedImageUri: Uri? = null
    private var isBotTyping = false

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { Log.d("PhotoPicker", "Selected URI: $uri"); attachImage(uri) }
        else { Log.d("PhotoPicker", "No media selected") }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) { Log.d("Camera", "Photo taken! URI: $tempCameraUri"); attachImage(tempCameraUri) }
        else { Log.d("Camera", "Photo capture failed") }
    }
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) { Log.d("Permission", "Camera permission granted"); launchCamera() }
        else { Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- 1. Database aur ViewModel Setup ---
        val apiKey = BuildConfig.OPENAI_API_KEY
        val dao = AppDatabase.getInstance(application).chatDao()
        val repo = ChatRepository(apiKey, applicationContext, dao)
        viewModel = ChatViewModel(repo)

        // --- 2. Main Chat Adapter Setup ---
        adapter = ChatAdapter(mutableListOf())
        binding.rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.rvChat.adapter = adapter

        // --- 3. Side Panel (Drawer) Adapter Setup ---
        historyAdapter = ChatHistoryAdapter { session ->
            // When user select chat from history
            viewModel.loadMessagesForSession(session.sessionId)
            binding.tvChatTitle.text = session.title // Title update karein
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.rvChatHistory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }

        // --- 4.Join Drawer with Action Bar ---
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.btnOpenDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // === 5. ViewModel Observers Setup ===
        setupViewModelObservers()

        // === 6. Button Click Listeners Setup ===
        setupClickListeners()

        // === 7. Whenever App Start ===
        viewModel.startNewChat() // Always start new chat
        viewModel.loadAllSessions() // Load Side panel
    }

    private fun setupViewModelObservers() {
        // Jab side panel ke liye sessions load hon
        viewModel.onSessionsLoaded = { sessions ->
            historyAdapter.submitList(sessions)
        }

        // When user intend to load previous chat
        viewModel.onMessagesLoaded = { messages ->
            adapter.setMessages(messages)
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)
        }

        // When user press '+' on screen new chat will be open
        viewModel.onNewChatCreated = {
            adapter.clearMessages()
            binding.tvChatTitle.text = "New Chat"
        }

        // "Thinking..."
        viewModel.onBotThinking = { message ->
            runOnUiThread {
                adapter.addMessage(Message(sessionId = "", text = message, isUser = false))
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                isBotTyping = true
            }
        }

        viewModel.onBotReply = { botMessage ->
            runOnUiThread {
                removeThinkingMessage()
                adapter.addMessage(botMessage)
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                // Whenever new message sent by user refresh history panel
                viewModel.loadAllSessions()
            }
        }

        // Error
        viewModel.onError = { err ->
            runOnUiThread {
                removeThinkingMessage()
                Toast.makeText(this, "Error: $err", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener { sendCurrentText() }

        binding.btnNewChat.setOnClickListener {
            viewModel.startNewChat()
        }

        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendCurrentText(); true } else false
        }
        setupCameraIconListener()
        binding.btnRemoveImage.setOnClickListener {
            removeAttachedImage()
        }
    }

    private fun removeThinkingMessage() {
        if (isBotTyping) {
            adapter.removeLastMessage()
            isBotTyping = false
        }
    }

    private fun sendCurrentText() {
        if (System.currentTimeMillis() - lastClickTime < 1000) return
        if (isBotTyping) {
            Toast.makeText(this, "Please wait, bot is processing...", Toast.LENGTH_SHORT).show()
            return
        }
        lastClickTime = System.currentTimeMillis()

        val text = binding.etMessage.text.toString().trim()
        val imageUri = attachedImageUri

        // Case 1: Check if image is attached
        if (imageUri != null) {
            val userMessage = Message(sessionId = "", text = text, isUser = true, imageUri = imageUri)
            adapter.addMessage(userMessage)
            viewModel.sendMessageWithImage(userMessage)
            removeAttachedImage(); binding.etMessage.setText("")
        }
        // Case 2: Check only for text
        else if (text.isNotEmpty()) {
            val userMessage = Message(sessionId = "", text = text, isUser = true, imageUri = null)
            adapter.addMessage(userMessage)
            viewModel.sendMessage(userMessage)
            binding.etMessage.setText("")
        }

        binding.rvChat.scrollToPosition(adapter.itemCount - 1)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupCameraIconListener() {
        binding.etMessage.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.etMessage.right - binding.etMessage.compoundDrawables[DRAWABLE_RIGHT].bounds.width() - binding.etMessage.paddingRight)) {
                    showImagePickDialog()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
    private fun showImagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this).setTitle("Select Image From").setItems(options) { _, which ->
            when (which) {
                0 -> { checkPermissionAndLaunchCamera() }
                1 -> { pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            }
        }.show()
    }
    private fun checkPermissionAndLaunchCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Permission", "Permission already granted, launching camera"); launchCamera()
            }
            else -> { Log.d("Permission", "Permission not granted, requesting..."); cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
        }
    }
    private fun launchCamera() {
        val uri = createImageUri()
        if (uri != null) { tempCameraUri = uri; takePictureLauncher.launch(uri) }
        else { Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show() }
    }
    private fun createImageUri(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(cacheDir, "images"); storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile)
    }
    private fun attachImage(uri: Uri?) {
        if (uri == null) return; attachedImageUri = uri
        binding.ivImagePreview.load(uri) { crossfade(true) }
        binding.imagePreviewLayout.visibility = View.VISIBLE
    }
    private fun removeAttachedImage() {
        attachedImageUri = null; binding.ivImagePreview.setImageDrawable(null)
        binding.imagePreviewLayout.visibility = View.GONE
    }
}