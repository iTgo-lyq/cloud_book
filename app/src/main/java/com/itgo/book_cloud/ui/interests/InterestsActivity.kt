package com.itgo.book_cloud.ui.interests

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.itgo.book_cloud.BookCloudApplication
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant.List_Tag_Book
import com.itgo.book_cloud.common.Constant.List_Tag_SelfDesc
import com.itgo.book_cloud.common.Constant.findTagValue
import com.itgo.book_cloud.common.setTimeout
import kotlinx.android.synthetic.main.activity_interest.*


class InterestsActivity : AppCompatActivity() {
    private val chipSelectedId2TagIdxMap = HashMap<Int, Int>()
    private val chipOptionId2TagIdxMap = HashMap<Int, Int>()
    private val chipTagIdx2SelectedIdMap = HashMap<Int, Int>()
    private val chipTagIdx2OptionIdMap = HashMap<Int, Int>()

    private lateinit var interestsViewModel: InterestsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        interestsViewModel = ViewModelProvider(
            this,
            InterestsViewModel.Factory(this)
        )[InterestsViewModel::class.java]

        for ((tagIdx, tagValue) in List_Tag_Book) {
            val id = ViewCompat.generateViewId()
            val chipView =
                layoutInflater.inflate(R.layout.chip_interest_choice, container, false) as Chip

            chipView.id = id
            chipView.text = tagValue
            chipView.setOnCheckedChangeListener(this::onChipCheckedChanged)

            chipOptionId2TagIdxMap[id] = tagIdx
            chipTagIdx2OptionIdMap[tagIdx] = id
            interestsViewModel.registerOption(tagIdx)
            favorChipGroup.addView(chipView)
        }

        for ((tagIdx, tagValue) in List_Tag_SelfDesc) {
            val id = ViewCompat.generateViewId()
            val chipView =
                layoutInflater.inflate(R.layout.chip_interest_choice, container, false) as Chip

            chipView.id = id
            chipView.text = tagValue
            chipView.setOnCheckedChangeListener(this::onChipCheckedChanged)

            chipOptionId2TagIdxMap[id] = tagIdx
            chipTagIdx2OptionIdMap[tagIdx] = id
            interestsViewModel.registerOption(tagIdx)
            descChipGroup.addView(chipView)
        }

        for ((tagIdx, isChecked) in interestsViewModel.selectedOptionMap.value ?: HashMap()) {
            val id = ViewCompat.generateViewId()
            val chipView =
                layoutInflater.inflate(R.layout.chip_interest_entry, container, false) as Chip

            chipView.id = id
            chipView.text = findTagValue(tagIdx)
            chipView.visibility = if (isChecked) View.VISIBLE else View.GONE
            chipView.setOnCloseIconClickListener(this::onChipDelete)

            chipSelectedId2TagIdxMap[id] = tagIdx
            chipTagIdx2SelectedIdMap[tagIdx] = id
            interestsViewModel.registerOption(tagIdx)
            selectedChipGroup.addView(chipView)
        }

        interestsViewModel.selectedOptionMap.observe(this) { map ->
            for ((tagIdx, isChecked) in map) {
                chipTagIdx2OptionIdMap[tagIdx]?.let {
                    val chipView = findViewById<Chip>(it)
                    chipView.isChecked = isChecked
                }
                chipTagIdx2SelectedIdMap[tagIdx]?.let {
                    val chipView = findViewById<Chip>(it)
                    chipView.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }
        }

        interestsViewModel.setTagsResult.observe(this) {
            if (it) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                setTimeout(1000) { finish() }
            }
        }

        interestsViewModel.networkError.observe(this) {
            if (!it.isNullOrEmpty()) Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onChipCheckedChanged(view: CompoundButton?, isChecked: Boolean) {
        view?.apply {
            interestsViewModel.updateOption(chipOptionId2TagIdxMap[id], isChecked)
        }
    }

    private fun onChipDelete(view: View) {
        view.apply {
            interestsViewModel.updateOption(chipSelectedId2TagIdxMap[id], false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.interest_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> interestsViewModel.save((application as BookCloudApplication).globalStore.userInfo.value?.uid)
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}