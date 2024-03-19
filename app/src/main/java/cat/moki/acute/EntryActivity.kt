package cat.moki.acute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts


class EntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AcuteApplication.application.defaultServer == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            startActivity(Intent(this, Home::class.java))

        }


    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            goHome()
        }
    }

    fun goHome() {
        if (AcuteApplication.application.defaultServer != null) {
        }
    }
}

