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
import android.provider.SearchRecentSuggestions
import android.widget.Toast
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    private lateinit var RecyclerViewAdapter: TodoList
    private lateinit var RecyclerViewManager: RecyclerView.LayoutManager
    private lateinit var todoList: RecyclerView

    var fileData = mutableListOf<String>()

    val filename = "tacoInstructions.txt"

    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001
    var URL: Uri? = null


    companion object{val REQUEST_IMAGE_CAPTURE = 1 }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val filename = "tacoInstruction.txt"
        openFileOutput(filename,Context.MODE_PRIVATE)

        val menu = openFileInput(filename).reader()

        RecyclerViewManager = LinearLayoutManager(this)
        RecyclerViewAdapter = TodoList(fileData)

        todoList = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = RecyclerViewManager
            adapter = RecyclerViewAdapter

            RecyclerViewAdapter.ClickItem = {string ->
                imageView.setImageURI(string.toUri())
            }
        }


        cameraButton.setOnClickListener {

            dispatchTakePictureIntent()


        }
        add.setOnClickListener {
            var fileContents = editText.text.toString() + "\n"

            openFileOutput(filename, Context.MODE_APPEND).use{
                it.write(fileContents.toByteArray())
            }

            RecyclerViewAdapter.insertTodoItems(fileContents)



            editText.text = null

        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                RecyclerViewAdapter.removeItem(viewHolder)
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        var items = RecyclerViewAdapter.list_items
        //println(items)

        openFileOutput(filename, Context.MODE_PRIVATE).use{
            for (item in items){
                it.write(item.toByteArray())
            }

        }




    }

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
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        URL = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, URL)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

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


