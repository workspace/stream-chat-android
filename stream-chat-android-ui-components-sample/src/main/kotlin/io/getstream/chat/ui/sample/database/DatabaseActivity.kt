package io.getstream.chat.ui.sample.database

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ActivityDatabaseBinding

class DatabaseActivity : AppCompatActivity() {

    private val binding by lazy { ActivityDatabaseBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<DatabaseViewModel> {
        DatabaseViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.deleteBtn.setOnClickListener {
            viewModel.onDeleteClick()
        }

        binding.generateBtn.setOnClickListener {
            viewModel.onGenerateClick()
        }

        binding.readBtn.setOnClickListener {
            viewModel.onReadClick()
        }

        binding.readManyBtn.setOnClickListener {
            viewModel.onReadManyClick()
        }

    }

}