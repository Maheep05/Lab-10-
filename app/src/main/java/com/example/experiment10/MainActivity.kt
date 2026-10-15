package com.example.experiment10

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var loadContactsButton: MaterialButton
    private lateinit var contactsListView: ListView
    private lateinit var contactsCountTextView: TextView

    private val contacts: MutableList<Contact> = mutableListOf()
    private lateinit var contactsAdapter: ContactsAdapter

    companion object {
        private const val READ_CONTACTS_REQUEST_CODE = 100
    }

    data class Contact(
        val id: String,
        val name: String,
        val number: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadContactsButton = findViewById(R.id.loadContactsButton)
        contactsListView = findViewById(R.id.contactsListView)
        contactsCountTextView = findViewById(R.id.contactsCountTextView)

        contactsAdapter = ContactsAdapter(this, contacts)
        contactsListView.adapter = contactsAdapter

        loadContactsButton.setOnClickListener {
            checkPermissionAndLoadContacts()
        }

        contactsListView.setOnItemClickListener { _, _, position, _ ->
            openContactInSystemApp(contacts[position])
        }
    }

    // -----------------------------
    // Permission handling
    // -----------------------------

    private fun checkPermissionAndLoadContacts() {

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            loadContacts()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == READ_CONTACTS_REQUEST_CODE) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                loadContacts()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot read contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // -----------------------------
    // Contacts Content Provider
    // -----------------------------

    private fun loadContacts() {

        contacts.clear()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {

            val nameIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )

            val numberIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val idIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            )

            val seenIds = HashSet<String>()

            while (it.moveToNext()) {

                val id = it.getString(idIndex) ?: continue
                if (!seenIds.add(id)) continue

                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: "No number"

                contacts.add(Contact(id, name, number))
            }
        }

        contactsAdapter.notifyDataSetChanged()

        val count = contacts.size
        contactsCountTextView.text =
            if (count == 1) "1 contact" else "$count contacts"

        if (contacts.isEmpty()) {
            Toast.makeText(
                this,
                "No contacts found on this device.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // -----------------------------
    // Data sharing: hand off contact URI
    // -----------------------------

    private fun openContactInSystemApp(contact: Contact) {

        val contactUri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_URI,
            contact.id
        )

        val intent = Intent(Intent.ACTION_VIEW, contactUri)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open contact.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // -----------------------------
    // Adapter
    // -----------------------------

    private class ContactsAdapter(
        private val context: Context,
        private val items: List<Contact>
    ) : BaseAdapter() {

        private val inflater: LayoutInflater =
            LayoutInflater.from(context)

        private val avatarPalette = listOf(
            R.color.avatar_1_bg to R.color.avatar_1_fg,
            R.color.avatar_2_bg to R.color.avatar_2_fg,
            R.color.avatar_3_bg to R.color.avatar_3_fg,
            R.color.avatar_4_bg to R.color.avatar_4_fg,
            R.color.avatar_5_bg to R.color.avatar_5_fg
        )

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Any = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup?
        ): View {

            val view = convertView ?: inflater.inflate(
                R.layout.item_contact,
                parent,
                false
            )

            val contact = items[position]

            val avatarView: TextView = view.findViewById(R.id.avatarTextView)
            val nameView: TextView = view.findViewById(R.id.nameTextView)
            val numberView: TextView = view.findViewById(R.id.numberTextView)

            val initial = contact.name
                .trim()
                .firstOrNull()
                ?.uppercaseChar()
                ?.toString()
                ?: "?"

            val (bgColorRes, fgColorRes) =
                avatarPalette[position % avatarPalette.size]

            val bg = avatarView.background
            if (bg is GradientDrawable) {
                bg.setColor(
                    ContextCompat.getColor(context, bgColorRes)
                )
            }

            avatarView.text = initial
            avatarView.setTextColor(
                ContextCompat.getColor(context, fgColorRes)
            )

            nameView.text = contact.name
            numberView.text = contact.number

            return view
        }
    }
}
