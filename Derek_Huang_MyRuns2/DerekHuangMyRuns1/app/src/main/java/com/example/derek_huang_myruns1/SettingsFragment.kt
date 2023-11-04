import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.CheckBox
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.derek_huang_myruns1.CommentsDialogFragment
import com.example.derek_huang_myruns1.MainActivity
import com.example.derek_huang_myruns1.R
import com.example.derek_huang_myruns1.UnitPreferenceDialogFragment


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Set click listeners for individual preferences
        findPreference<Preference>("user_profile")?.setOnPreferenceClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            true
        }

// Couldn't figure out
//        findPreference<Preference>("privacy_setting")?.setOnPreferenceClickListener {
//            // Handle the click for "privacy_setting" preference
//            val checkBox = findViewById<CheckBox>(R.id.myCheckBox)
//            checkbox.isChecked = !checkbox.isChecked
//            true
//        }

        findPreference<Preference>("unit_preference")?.setOnPreferenceClickListener {
            val myDialog = UnitPreferenceDialogFragment()
            myDialog.show(requireFragmentManager(), "tag")
            true
        }

        findPreference<Preference>("comments")?.setOnPreferenceClickListener {
            val myDialog = CommentsDialogFragment()
            myDialog.show(requireFragmentManager(), "tag")
            true
        }

        findPreference<Preference>("webpage")?.setOnPreferenceClickListener {
            val url = "https://www.sfu.ca/computing.html"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        }
    }
}
