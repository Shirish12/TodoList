package com.example.tacos

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    // Late initializing the variables to use them later in the code
    // Ref: https://www.youtube.com/watch?v=hyyX3g57Ms8&t=720s

    private lateinit var RecyclerViewAdapter: TodoList
    private lateinit var RecyclerViewManager: RecyclerView.LayoutManager
    private lateinit var todoList: RecyclerView

    //Creating an empty mutable list which will hold the file content

    var fileData = mutableListOf<String>()

    val filename = "tacoInstructions.txt"

    // Image capture permission code for the OS

    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001

    // Creating an empty variable to save the path of captured image
    var URL: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Opening the file to grab the file content and provide it to the RecyclerView
        openFileOutput(filename, Context.MODE_APPEND)
        val menu = openFileInput(filename).reader()

        //Reading each line of file and creating it as a mutablelist
        fileData = menu.readLines() as MutableList<String>

        // Initialization of the recycler view
        // Ref:https://www.youtube.com/watch?v=hyyX3g57Ms8&t=720s
        RecyclerViewManager = LinearLayoutManager(this)
        RecyclerViewAdapter = TodoList(fileData)

        todoList = findViewById<RecyclerView>(R.id.recyclerView).apply {

            // RecyclerView size is fixed so it becomes scrollable and doesn't increase
            setHasFixedSize(true)
            layoutManager = RecyclerViewManager
            adapter = RecyclerViewAdapter

            // RecyclerView onclicklistener: Invokes ClickItem when any item inside the recyclerview is clicked
            // when clicked on Image url it shows the image in the imageview
            RecyclerViewAdapter.ClickItem = {string ->
                imageView.setImageURI(string.toUri())
            }
        }

        // Opens camera to snap the image
        cameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }


        // Adds the item in the Todo List and also displays it in the recyclerview
        add.setOnClickListener {

            var fileContents = editText.text.toString() + "\n"

            // This writes the content from textview in to the file
            openFileOutput(filename, Context.MODE_APPEND).use{
                it.write(fileContents.toByteArray())
            }

            //Adds the content in the recyclerview
            RecyclerViewAdapter.insertTodoItems(fileContents)
            editText.text = null
        }

        // This function deletes the item from the Recyclerview and also from the file when swiped
        //Ref: https://www.youtube.com/watch?v=eEonjkmox-0&t=1626s
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {

                // Removes the item from recyclerview if swiped right or left
                RecyclerViewAdapter.removeItem(viewHolder)
                var items = RecyclerViewAdapter.list_items

                deleteItem(items)
            }

        }

        // Calls the above method so when the user swipes it deletes that particular item
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }


    // This function deletes the items from the file itself
    fun deleteItem(items: MutableList<String>) {
        openFileOutput(filename, Context.MODE_PRIVATE).use {
            for (item in items) {
                var item = item + "\n"
                it.write(item.toByteArray())
            }

        }
    }

    // This functions asks for the camera permission and external storage permission
    // Ref: https://devofandroid.blogspot.com/2018/09/take-picture-with-camera-android-studio_22.html
    private fun dispatchTakePictureIntent(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
                //permission was not enabled
                val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            }
            else{
                //permission already granted
                openCamera()
            }
        }
        else{
            //system os is < marshmallow
            openCamera()
        }
    }

    //This function opens up the camera and saves the captured image path in URL variable
    // Ref: https://devofandroid.blogspot.com/2018/09/take-picture-with-camera-android-studio_22.html
    private fun openCamera() {
        val values = ContentValues()
        URL = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, URL)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    // This function opens the camera after receiving the required permissions
    // Ref: https://devofandroid.blogspot.com/2018/09/take-picture-with-camera-android-studio_22.html
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    // This function saves the captured image in the external storage and saves the image path in URL Variable
    // Ref: https://devofandroid.blogspot.com/2018/09/take-picture-with-camera-android-studio_22.html
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //called when image was captured from camera intent
        if (resultCode == Activity.RESULT_OK){
            //set image captured to image view
            imageView.setImageURI(URL)
            var imagepath:String = ""

            imagepath = "" + URL

            openFileOutput(filename, Context.MODE_APPEND).use{
                it.write(imagepath.toByteArray())
            }

            RecyclerViewAdapter.insertTodoItems(imagepath)


        }
    }


    }


