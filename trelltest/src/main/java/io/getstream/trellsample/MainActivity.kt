package io.getstream.trellsample

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf

class MainActivity : AppCompatActivity() {

    private val userGroupsContainer: ViewGroup
        get() = findViewById(R.id.user_groups)

    private val trendyGroupsContainer: ViewGroup
        get() = findViewById(R.id.trendy_groups)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(userGroupsContainer.id, UsersGroupFragment().apply { arguments = bundleOf() }, "userGroups")
            .replace(
                trendyGroupsContainer.id,
                TrendyGroupsFragment().apply {
                    arguments = bundleOf("show_header" to false, "show_search" to false)
                },
                "trendyGroups"
            )
            .commit()
    }
}
