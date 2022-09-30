package com.saloni.videoPlayer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.saloni.videoPlayer.databinding.ActivityMainBinding
import java.io.File
import kotlin.system.exitProcess
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
    }

    class VideoPickerActivity(val columnIndex: Int) : FragmentActivity() {


        private var TAG = "VideoPickerActivity"

        private var SELECT_VIDEO = 1

        private lateinit var selectedVideos: List<String>



        // lateinit var textView: TextView

        private lateinit var binding: ActivityMainBinding
        private lateinit var toggle: ActionBarDrawerToggle







        @SuppressLint("ObsoleteSdkInt", "ObjectAnimatorBinding", "IntentReset")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            //   textView=findViewById(R.id.txtHello)


            binding = ActivityMainBinding.inflate(layoutInflater)
            setTheme(R.style.coolPinkNav)
            setContentView(binding.root)
            //for Nav Drawer
            toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
            binding.root.addDrawerListener(toggle)
            toggle.syncState()
            //supportActionBar?.setDisplayHomeAsUpEnabled(true)
            if (requestRuntimePermission()) {
                folderList = ArrayList()
                videoList = getAllVideos()
                setFragment(VideosFragment())
            }
            binding.bottomNav.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.videoView -> setFragment(VideosFragment())
                    R.id.foldersView -> setFragment(FoldersFragment())
                }
                return@setOnItemSelectedListener true
            }
            binding.navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.aboutNav -> Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                    R.id.exitNav -> exitProcess(1)
                }
                return@setNavigationItemSelectedListener true
            }

        }
        private fun setFragment(fragment:Fragment){
            val transaction= supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentFL, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }



        //for requesting permission
        private fun requestRuntimePermission(): Boolean {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 13)
                return false
            }
            return true
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 13) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    folderList = ArrayList()
                    videoList = getAllVideos()
                    setFragment(VideosFragment())
                } else
                    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 13)
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (toggle.onOptionsItemSelected(item))
                return true
            return super.onOptionsItemSelected(item)
        }

        @SuppressLint("InlinedApi", "Recycle", "Range")
        private fun getAllVideos(): ArrayList<Video> {
            val tempList = ArrayList<Video>()
            val tempFolderList = ArrayList<String>()
            val projection = arrayOf(
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_ID
            )
            val cursor = this.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC"
            )
            if (cursor != null)
                if (cursor.moveToNext())
                    do {
                        val titleC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                        val idC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                        val folderC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                        val folderIdC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                        val sizeC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                        val pathC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                        val durationC =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                                .toLong()

                        try {
                            val file = File(pathC)
                            val artUriC = Uri.fromFile(file)
                            val video = Video(
                                title = titleC,
                                id = idC,
                                folderName = folderC,
                                duration = durationC,
                                size = sizeC,
                                path = pathC,
                                artUri = artUriC
                            )
                            if (file.exists()) tempList.add(video)


                            if (!tempFolderList.contains(folderC)) {
                                tempFolderList.add(folderC)
                                folderList.add(Folder(id = folderIdC, folderName = folderC))
                            }
                        } catch (e: Exception) {
                        }
                    } while (cursor.moveToNext())
            cursor?.close()
            return tempList
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_VIDEO) {
                    selectedVideos = getSelectedVideos(data) as List<String>
                }
            }
            finish()
        }

        private fun getSelectedVideos(data: Intent?): List<String?> {
            val result: MutableList<String?> = ArrayList()
            val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)

            // Single video selected
            if (data?.data != null) {
                val mImageUri = data.data

                // Get the cursor
                val cursor: Cursor? = contentResolver.query(
                    mImageUri!!,
                    filePathColumn, null, null, null
                )
                // Move to first row
                cursor?.moveToFirst()
                val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn[0])
                val videoPath: String? = columnIndex?.let { cursor?.getString(it) }
                if (videoPath == null) Log.e(TAG, "videoPath is null")
                result.add(videoPath)
                cursor?.close()
            } else {
                val mClipData = data?.clipData
                if (mClipData != null) {
                    for (i in 0 until mClipData.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val uri = item.uri
                        // Get the cursor
                        val cursor: Cursor? =
                            contentResolver.query(uri, filePathColumn, null, null, null)
                        // Move to first row
                        if (cursor == null) Log.e(TAG, "cursor is null") else {
                            if (cursor.moveToFirst()) {
                                val columnIndex: Int =
                                    cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                                val videoPath: String = cursor.getString(columnIndex)
                                if (videoPath == null) Log.e(TAG, "videoPath is null")
                                result.add(videoPath)
                            } else {
                                Log.e(TAG, "cannot use cursor")
                            }
                            cursor.close()
                        }
                    }
                }
            }
            return result
        }
    }




}





