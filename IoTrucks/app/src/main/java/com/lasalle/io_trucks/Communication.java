package com.lasalle.io_trucks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

/**
 * @file Communication.java
 * @author Mathieu Arthur
 * @version 0.1
 * @brief Déclaration de la classe Communication
 */

/**
 * @class Communication
 * @brief Classe de Communication et de connexion bluetooth
 */
public class Communication
{
    /**
     * Constantes
     */
    private static final String TAG = "Communication";
    /**
     * Attributs
     */
    private BroadcastReceiver receiverEtatBluetooth;
    private BroadcastReceiver receiverScan;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> listeAppareilConnus;

    /**
     * @brief Vérifie si le bluetooth est disponible et activé, sinon demande l'autorisation de l'activé
     * @param contextAcceuil contexte de l'acceuil
     */
    public void demanderActivationBluetooth(Context contextAcceuil)
    {
        if (bluetoothAdapter == null)
        {
            Log.i(TAG,"demanderActivationBluetooth() bluetoothAdapter = null");
            Toast.makeText(contextAcceuil, R.string.str_bluetoot_inexistant, Toast.LENGTH_SHORT).show();
        }
        else if (!bluetoothAdapter.isEnabled())
        {
            Log.i(TAG,"demanderActivationBluetooth() bluetooth désactivé");
            Toast.makeText(contextAcceuil, R.string.str_bluetooth_eteint, Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) contextAcceuil).startActivityForResult(enableBtIntent,1);
        }
        else
        {
            Log.i(TAG,"demanderActivationBluetooth() bluetooth activé");
            Toast.makeText(contextAcceuil, R.string.str_bluetooth_allumer, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @brief Vérifie les modification d'état du bluetooth
     * @return renvoie le Receiver de l'état du bluetooth
     */
    public BroadcastReceiver ecouterEtatBluetooth()
    {
        receiverEtatBluetooth = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                final String action = intent.getAction();
                Log.i(TAG,"onReceive() " + action);
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state)
                    {
                        case BluetoothAdapter.STATE_OFF:
                            Log.i(TAG,"onReceive() Bluetooth désactivé !");
                            Toast.makeText(context, R.string.str_bluetooth_eteint, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Toast.makeText(context, R.string.str_bluetooth_eteint_en_cours, Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.i(TAG,"onReceive() Bluetooth activé !");
                            Toast.makeText(context, R.string.str_bluetooth_allumer, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.i(TAG,"onReceive() Bluetooth s'active !");
                            Toast.makeText(context, R.string.str_bluetooth_allumer_en_cours, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        };

        return receiverEtatBluetooth;
    }

    /**
     * @brief Méthode de recherche des appareils qui ont déjà était appairer.
     * @param contextAcceuil Contient le contexte de l'activité Acceuil
     */
    public void rechercherAppareilConnu(Context contextAcceuil)
    {
        listeAppareilConnus = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice blueDevice : listeAppareilConnus)
        {
            Toast.makeText(contextAcceuil, "Appareil " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @brief Méthode qui retourne l'appareil Bluetooth io-trucks
     * @param nomAppareil le nom de l'appareil Bluetooth io-trucks
     * @return Renvoie le device utilisé pour la communication
     */
    public BluetoothDevice recupererAppareilBluetooth(String nomAppareil)
    {
        listeAppareilConnus = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice blueDevice : listeAppareilConnus)
        {
            if(blueDevice.getName().equals(nomAppareil))
            {
                Log.d(TAG, "recupererAppareilBluetooth() io-trucks trouvé : " + blueDevice.getName() + " (" + blueDevice.getAddress() + ")");
                return blueDevice;
            }
        }
        return null;
    }

    /**
     * @brief Méthode pour unregister les receiver à la destruction de l'application
     * @param contextAcceuil contexte de l'acceuil
     */
    public void unregisterBluetooth(Context contextAcceuil)
    {
        if (bluetoothAdapter != null)
        {
            bluetoothAdapter.cancelDiscovery();
            contextAcceuil.unregisterReceiver(receiverEtatBluetooth);
        }
    }
}
