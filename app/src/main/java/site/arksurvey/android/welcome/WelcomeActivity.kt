package site.arksurvey.android.welcome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import site.arksurvey.android.databinding.ActivityWelcomeBinding
import site.arksurvey.android.safeLazy

class WelcomeActivity : AppCompatActivity() {
    private val binding by safeLazy { ActivityWelcomeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.reset.setOnClickListener {
            val value = "${binding.reset.text}_${binding.welcomeToolbar.title}"
            Snackbar.make(binding.reset, value, Snackbar.LENGTH_SHORT).show()
        }
        binding.showAlertWindowGlobalWithAccessibility.setOnClickListener {
            val value =
                "${binding.showAlertWindowGlobalWithAccessibility.text}_${binding.welcomeToolbar.title}"
            Snackbar.make(it, value, Snackbar.LENGTH_SHORT).show()
        }
    }
}