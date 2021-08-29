package com.mp.chatassignmentforalminds

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.JsonElement
import com.mp.chatassignmentforalminds.adapter.ChatItemsAdapter
import com.mp.chatassignmentforalminds.databinding.ActivityMainBinding
import com.mp.chatassignmentforalminds.dto.ChatDto
import com.mp.chatassignmentforalminds.requestHandler.ApiClient
import com.mp.chatassignmentforalminds.requestHandler.ApiInterface
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myReceiver:MyReceiver
    private val chatList by lazy { ArrayList<String>() }
    private val chatAdapter by lazy { ChatItemsAdapter(chatList) }
    val CHAT_BROADCAST = "${PACKAGE_NAME}.broadcast"
    val EXTRA_CHAT_DATA = "$PACKAGE_NAME.chat_data"
    enum class USERS{
        userA,userB
    }
    private var apiClient: ApiInterface = ApiClient.getClient().create(ApiInterface::class.java)
    private var loggedInUser:USERS?=null
    private val fcmServerKey="key=AAAAwKlOuE8:APA91bGKrGaK2Fkly86hoVNLlximH51dI7cCFBjSWAO0v3IHle48TDxioAhsj9z_N5bsa_mkGddwesc9fpreTB-mmMzwcuf23I-zyhCJF2-vqdcH5CTqxQABjt62qTTKDbpFr1Gp37Pl"
    private val TAG = "MainActivityTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myReceiver = MyReceiver()
        Firebase.messaging.subscribeToTopic(getString(R.string.topic_name))
            .addOnCompleteListener { task ->
                var msg = "Subscribed to chat room"
                if (!task.isSuccessful) {
                    msg = getString(R.string.msg_subscribe_failed)
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }

        binding.rvChat.adapter=chatAdapter

        binding.btnUserA.setOnClickListener {
            binding.btnUserA.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(this,R.color.button_active))
            binding.btnUserB.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(this,R.color.button_inactive))
            loggedInUser=USERS.userA
        }
        binding.btnUserB.setOnClickListener {
            binding.btnUserB.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(this,R.color.button_active))
            binding.btnUserA.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(this,R.color.button_inactive))
            loggedInUser=USERS.userB
        }
        binding.btnLogout.setOnClickListener {
            finish()
        }
        binding.btnSendChat.setOnClickListener {
            if(loggedInUser!=null){
                if(binding.edtChatField.text.toString().isNotEmpty()){
                sendChat()
                    hideKeyboard()
                }
                else
                    Toast.makeText(this,"empty field",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"no user logged in",Toast.LENGTH_SHORT).show()
            }

        }


    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver,
            IntentFilter(CHAT_BROADCAST)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
    }

    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: 1")

            val chatData =
                intent.getStringExtra(EXTRA_CHAT_DATA)
            if (chatData != null) {
                loadChat(chatData)
                Log.d(TAG, "onReceive: 2")
                Log.d(TAG, "onReceive: -> $chatData")

        }
    }

    private fun loadChat(chatData:String){
        val json = JSONObject(chatData)
       // val data = json.getJSONObject("data")
        var sender = json.getString("sender")
        sender=sender.removePrefix("user")
        val message = json.getString("message")
        val tempChat="$sender:$message"
        //if(loggedInUser.toString()!=sender) {
            chatList.add(tempChat)
            chatAdapter.notifyItemInserted(chatList.size)
       // }
    }
    }

    private fun sendChat(){
        if(isOnline()) {
            var reciver = USERS.userB.toString()
            if (loggedInUser == USERS.userB)
                reciver = USERS.userA.toString()
            val postDate = ChatDto(
                to = "/topics/chat_room1",
                chatData = ChatDto.ChatData(
                    binding.edtChatField.text.toString(),
                    reciver,
                    loggedInUser.toString()
                )

            )
            binding.btnSendChat.isVisible=false
            binding.pbar.isVisible=true
            apiClient.sendChat(postDate, fcmServerKey).enqueue(object :
                Callback<JsonElement> {
                override fun onResponse(
                    call: Call<JsonElement>,
                    response: Response<JsonElement>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@MainActivity,"Something went wronf",Toast.LENGTH_SHORT).show()
                    }
                    binding.pbar.isVisible=false
                    binding.btnSendChat.isVisible=true
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {

                }
            })
        }else{
            Toast.makeText(this,"no Internet connection",Toast.LENGTH_SHORT).show()
        }

    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        //Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        //Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        //Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = this.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}