/**
 * ownCloud Android client application
 *
 * @author David Crespo Ríos
 * Copyright (C) 2022 ownCloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.presentation.settings.advanced

import android.app.Activity
import android.os.Bundle
import android.security.KeyChain
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.owncloud.android.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsAdvancedFragment : PreferenceFragmentCompat() {

    // ViewModel
    private val advancedViewModel by viewModel<SettingsAdvancedViewModel>()

    private var prefShowHiddenFiles: SwitchPreferenceCompat? = null
    private var prefSelectClientCert: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_advanced, rootKey)

        prefShowHiddenFiles = findPreference(PREF_SHOW_HIDDEN_FILES)
        prefSelectClientCert = findPreference(PREF_SELECT_CLIENT_CERT)

        initPreferenceListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefShowHiddenFiles?.isChecked = advancedViewModel.isHiddenFilesShown()
    }

    private fun initPreferenceListeners() {
        prefShowHiddenFiles?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            advancedViewModel.setShowHiddenFiles(newValue as Boolean)
            true
        }

        prefSelectClientCert?.apply {
            summary = advancedViewModel.getSelectedClientCert() ?: context.resources.getString(R.string.prefs_select_client_cert_not_selected)

            setOnPreferenceClickListener {
                KeyChain.choosePrivateKeyAlias(
                    context as Activity,
                    { s -> run {
                        advancedViewModel.setSelectedClientCert(s);
                        summary = s ?: context.resources.getString(R.string.prefs_select_client_cert_not_selected)
                    }}, null, null, null, -1, null
                )

                true
            }
        }
    }

    companion object {
        const val PREF_SHOW_HIDDEN_FILES = "show_hidden_files"
        const val PREF_SELECT_CLIENT_CERT = "select_client_cert"
    }
}
