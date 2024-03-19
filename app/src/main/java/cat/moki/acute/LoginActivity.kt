package cat.moki.acute

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import cat.moki.acute.client.LocalClient
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.login.Login
import cat.moki.acute.components.login.State
import cat.moki.acute.models.Credential
import cat.moki.acute.ui.theme.AcuteTheme
import okhttp3.HttpUrl.Companion.toHttpUrl


class LoginActivity : AppCompatActivity() {
    private var fromSetting = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "onCreate: ?")
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        fromSetting = intent.getBooleanExtra("FromSetting", false)

        setContent {
            AcuteTheme {
                Surface {
                    Login {

                        NetClient.request(it).ping().run {
                            return@Login body()?.run {
                                if (!isSuccessful) {
                                    State.InternetError
                                } else if (subsonicResponse.status == "ok") {
                                    AcuteApplication.application.run {
                                        addAccount(it)
                                        initServers()
                                    }
                                    setResult(RESULT_OK, intent);
                                    if (!fromSetting) {
                                        startActivity(Intent(this@LoginActivity, Home::class.java))
                                    }
                                    finish()
                                    State.Success
                                } else {
                                    State.Failed
                                }
                            } ?: run {
                                State.InternetError
                            }
                        }

                    }
                }
            }
        }
    }


}