package com.example.todolist


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.todolist.databinding.ActivityHomePageBinding
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var adapter: TabsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ViewPager with Adapter
        adapter = TabsPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = adapter

        // Handle ViewPager swiping
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.tabLayout.setItemSelected(R.id.nav_completed, true)
                    1 -> binding.tabLayout.setItemSelected(R.id.nav_pending, true)
                    2 -> binding.tabLayout.setItemSelected(R.id.nav_overdue, true)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        // Handle ChipNavigationBar click events
        binding.tabLayout.setOnItemSelectedListener { id ->
            when (id) {
                R.id.nav_completed -> binding.viewPager.currentItem = 0
                R.id.nav_pending -> binding.viewPager.currentItem = 1
                R.id.nav_overdue -> binding.viewPager.currentItem = 2
            }
        }

        // Set default selection
        binding.tabLayout.setItemSelected(R.id.nav_pending, true)

        // Open AddNewTask Activity on button click
        binding.btn.setOnClickListener {
            val intent = Intent(this, AddNewTask::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_TASK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("title")
            val description = data?.getStringExtra("description")
            val deadline = data?.getStringExtra("deadline")

            if (title != null && description != null && deadline != null) {
                Log.d("HomePageActivity", "Received task: $title, $description, $deadline")

                val pendingFragment = adapter.getPendingFragment()
                if (pendingFragment != null) {
                    pendingFragment.addTask(Task(title, description, deadline))
                } else {
                    Log.e("HomePageActivity", "❌ PendingFragment still not found!")
                }
            } else {
                Log.e("HomePageActivity", "❌ Data is missing!")
            }
        }
    }


    private fun getPendingFragment(): PendingFragment? {
        return supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.view_pager + ":1") as? PendingFragment
    }


    companion object {
        private const val REQUEST_CODE_ADD_TASK = 100
    }
}
