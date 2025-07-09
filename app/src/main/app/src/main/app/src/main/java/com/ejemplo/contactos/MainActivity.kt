package com.ejemplo.contactos

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    val nombreContacto = "PagoMovil"
    val numeroContacto = "+5355555555"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btnCrearActualizar)

        btn.setOnClickListener {
            if (tienePermisos()) {
                crearOActualizarContacto(nombreContacto, numeroContacto)
            } else {
                pedirPermisos()
            }
        }
    }

    private fun tienePermisos(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pedirPermisos() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS
            ),
            1
        )
    }

    private fun buscarContactoPorNombre(nombre: String): Long? {
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data.CONTACT_ID),
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} = ?",
            arrayOf(nombre),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
            }
        }
        return null
    }

    private fun borrarContacto(id: Long) {
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
        val ops = ArrayList<ContentProviderOperation>()
        ops.add(ContentProviderOperation.newDelete(uri).build())
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun crearOActualizarContacto(nombre: String, numero: String) {
        val idExistente = buscarContactoPorNombre(nombre)
        if (idExistente != null) {
            borrarContacto(idExistente)
        }

        val ops = ArrayList<ContentProviderOperation>()

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, nombre)
                .build()
        )

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numero)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        )

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            Toast.makeText(this, "Contacto actualizado o creado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al crear/actualizar el contacto", Toast.LENGTH_SHORT).show()
        }
    }
}
