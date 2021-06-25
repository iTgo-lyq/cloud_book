package com.itgo.book_cloud.ui.home

import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.itgo.book_cloud.BookCloudApplication
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant.Application_Schema
import com.itgo.book_cloud.common.Constant.Flag_User_NoTags_With_Age
import com.itgo.book_cloud.common.parseApplicationClipString
import com.itgo.book_cloud.common.setImageURI
import com.itgo.book_cloud.common.setTimeout
import com.itgo.book_cloud.ui.SplashActivity
import com.itgo.book_cloud.ui.UploadActivity
import com.itgo.book_cloud.ui.interests.InterestsActivity
import com.itgo.book_cloud.ui.login.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.drawer_header_home.*


class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {
    private val clipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }

    private lateinit var navController: NavController
    private var confirmRedirectToLoginAlert: AlertDialog? = null
    private var confirmRedirectToInterestsAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController =
            (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController

        confirmRedirectToLoginAlert = MaterialAlertDialogBuilder(this)
            .setMessage("为体验完整内容，即将前往登录～")
            .setPositiveButton("确认") { _, _ ->
                val intent = Intent()
                intent.setClass(this, LoginActivity::class.java)
                startActivity(intent)
            }.setOnCancelListener {
                val intent = Intent()
                intent.setClass(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .create()

        confirmRedirectToInterestsAlert = MaterialAlertDialogBuilder(this)
            .setMessage("请选择你的标签，橙心将会为您提供定制化服务～")
            .setPositiveButton("确认") { _, _ ->
                val intent = Intent()
                intent.setClass(this, InterestsActivity::class.java)
                startActivity(intent)
            }.setOnCancelListener {
                val intent = Intent()
                intent.setClass(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .create()

        drawerNavigation.setNavigationItemSelectedListener(this)
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        navController.addOnDestinationChangedListener(this)

        avatarHome.setOnClickListener(this::openDrawer)

        checkShouldRedirect()
    }

    override fun onResume() {
        super.onResume()
        readClipData()
    }

    private fun checkShouldRedirect() {
        val userInfo = (application as BookCloudApplication).globalStore.userInfo.value

        if (userInfo == null) {
            confirmRedirectToLoginAlert?.show()
            confirmRedirectToLoginAlert = null
        } else if (userInfo.age == Flag_User_NoTags_With_Age) {
            confirmRedirectToInterestsAlert?.show()
            confirmRedirectToInterestsAlert = null
        }
    }

    private fun openDrawer(view: View) {
        drawerLayout.openDrawer(GravityCompat.START)

        (application as BookCloudApplication).globalStore.userInfo.value.let {
            nickname.text = it?.nickname
            avatarDrawer.setImageURI(it?.portrait, R.drawable.i_avatar)
        }
    }

    private fun closeDrawer(id: Int) {
        if (id in listOf(R.id.nav_interests, R.id.nav_upload)) drawerLayout.closeDrawers()
    }

    private fun interceptNavigation(id: Int) {
        if (navController.currentDestination?.id == id) return

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
            .build()

        navController.navigate(id, null, options)
    }

    private fun readClipData() {
        setTimeout(this, 500) {
            val clipData = clipboardManager.primaryClip
            val item = clipData?.getItemAt(0)
            val uri = parseApplicationClipString(item)
            if (uri?.scheme == Application_Schema) {
                val segments = uri.pathSegments
                when (segments[0]) {
                    "share" -> {
                        if (segments[1] != (application as BookCloudApplication).globalStore.userInfo.value?.uid.toString() && segments[2] != null)
                            createShareAlertDialog(segments[2], item?.text.toString()).show()
                    }
                }
            }
        }
    }

    private fun createShareAlertDialog(cbid: String, msg: String?) =
        MaterialAlertDialogBuilder(this)
            .setTitle("来自某好人的分享")
            .setMessage(msg ?: "")
            .setNeutralButton("取消") { _, _ -> }
            .setPositiveButton("添加到书架") { _, _ ->

            }.create()


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_toolbar_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_bookshelf -> {

            }
            R.id.nav_interests -> {

            }
        }

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_folder -> interceptNavigation(R.id.folderFragment)
            R.id.nav_bookshelf -> interceptNavigation(R.id.bookShelfFragment)
            R.id.nav_recommend -> interceptNavigation(R.id.recommendFragment)
            R.id.nav_interests -> {
                setTimeout(250) {
                    val intent = Intent()
                    intent.setClass(this, InterestsActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.nav_upload -> {
                setTimeout(250) {
                    val intent = Intent()
                    intent.setClass(this, UploadActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.nav_quit -> {
                (application as BookCloudApplication).quit()
                val intent = Intent()
                intent.setClass(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        closeDrawer(item.itemId)

        return true
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.folderFragment -> bottomNavigation.selectedItemId = R.id.nav_folder
            R.id.bookShelfFragment -> bottomNavigation.selectedItemId = R.id.nav_bookshelf
            R.id.recommendFragment -> bottomNavigation.selectedItemId = R.id.nav_recommend
        }
    }
}