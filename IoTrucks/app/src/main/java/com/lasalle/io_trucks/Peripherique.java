package com.lasalle.io_trucks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @file Peripherique.java
 * @author Mathieu Arthur
 * @version 0.1
 * @brief Déclaration de la classe Peripherique
 */

/**
 * @class Peripherique
 * @brief Classe permettant de gérer les périphériques.
 */
public class Peripherique extends Thread
{
    /**
     * Constantes
     */
    private static final String TAG = "Peripherique";
    public final static int CODE_CONNEXION = 0;
    public final static int CODE_RECEPTION = 1;
    public final static int CODE_EMISSION = 2;
    public final static int CODE_DECONNEXION = 3;
    /**
     * Attributs
     */
    private String nom;
    private String adresse;
    private Handler handler = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private InputStream receiveStream = null;
    private OutputStream sendStream = null;
    private TReception treception = null;

    /**
     * @brief Constructeur de la classe Périphérique
     * @param device Définis l'appareil associé
     * @param handler Définis la gestion du thread
     */
    public Peripherique(BluetoothDevice device, Handler handler)
    {
        if (device != null)
        {
            this.device = device;
            this.nom = device.getName();
            this.adresse = device.getAddress();
            this.handler = handler;
        }
        else
        {
            this.device = device;
            this.nom = "Aucun";
            this.adresse = "";
            this.handler = handler;
        }

        creerSocket();
    }

    /**
     * @brief Méthode de création du socket bluetooth
     */
    private void creerSocket()
    {
        try
        {
            if(device != null)
            {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                receiveStream = socket.getInputStream();
                sendStream = socket.getOutputStream();
            }
        }
        catch (IOException e)
        {
            Log.d(TAG, "Erreur createRfcommSocketToServiceRecord()");
            e.printStackTrace();
            socket = null;
        }
        if (socket != null)
        {
            treception = new TReception(handler);
        }
    }

    /**
     * @brief Méthode pour obtenir le nom du périphérique
     * @return Renvoie le nom du périphérique
     */
    public String getNom()
    {
        return nom;
    }

    /**
     * @brief Méthode pour obtenir l'adresse du périphérique
     * @return Renvoie l'adresse du périphérique
     */
    public String getAdresse()
    {
        return adresse;
    }

    /**
     * @brief Méthode perméttant de savoir si on est connecter
     * @return Renvoie un booléen définissant l'état
     */
    public boolean estConnecte()
    {
        return socket.isConnected();
    }

    /**
     * @brief Méthode perméttant de set le nom du périphérique
     * @param nom définis le nom du périphérique
     */
    public void setNom(String nom)
    {
        this.nom = nom;
    }

    /**
     * @brief Méthode perméttant de renvoyer le périphérique en un String
     * @return Renvoie un String contennant le nom et l'adresse du périphérique
     */
    public String toString()
    {
        return "\nNom : " + nom + "\nAdresse : " + adresse;
    }

    /**
     * @brief Méthode perméttant d'envoyer une trame à l'aide du Thread
     * @param data Représente les données à envoyer
     */
    public void envoyer(final String data)
    {
        if (socket == null)
            return;

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if (socket.isConnected())
                    {
                        sendStream.write(data.getBytes());
                        sendStream.flush();
                        Message msg = Message.obtain();
                        msg.what = CODE_EMISSION;
                        msg.obj = data;
                        handler.sendMessage(msg);
                    }
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Erreur write()");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * @brief Méthode perméttant de se connecter à un périphérique
     */
    public void connecter()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                if (connecterSocket())
                {
                    return;
                }
                // sinon reconnexion
                creerSocket();
                connecterSocket();

            }
        }.start();
    }

    /**
     * @brief Méthode perméttant de connecter le socket
     * @return Renvoie un booléen afin de savoir si le socket est connecter
     */
    private boolean connecterSocket()
    {
        try
        {
            if(socket == null)
                return false;
            if (!socket.isConnected())
            {
                socket.connect();
                Message msg = Message.obtain();
                msg.what = CODE_CONNEXION;
                handler.sendMessage(msg);

                treception.start();
                return true;
            }
        }
        catch (IOException e)
        {
            Log.d(TAG, "Erreur connect()");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @brief Méthode perméttant de se déconnecter du périphérique
     * @return Renvoie un booléen afin de savoir si on est bien déconnecter
     */
    public boolean deconnecter()
    {
        try
        {
            treception.arreter();

            socket.close();
            Message msg = Message.obtain();
            msg.what = CODE_DECONNEXION;
            handler.sendMessage(msg);

            return true;
        }
        catch (IOException e)
        {
            Log.d(TAG, "Erreur close()");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @class TReception
     * @brief Déclaration de la classe TReception
     */
    private class TReception extends Thread
    {
        Handler handlerUI;
        private boolean fini;

        TReception(Handler h)
        {
            handlerUI = h;
            fini = false;
        }

        @Override
        public void run()
        {
            Log.d(TAG, "TReception run() start");
            BufferedReader reception = new BufferedReader(new InputStreamReader(receiveStream));
            while (!fini)
            {
                try
                {
                    String trame = "";
                    if (reception.ready())
                    {
                        trame = reception.readLine();
                    }
                    if (trame.length() > 0)
                    {
                        Log.d(TAG, "run() trame : " + trame);
                        Message msg = Message.obtain();
                        msg.what = Peripherique.CODE_RECEPTION;
                        msg.obj = trame;
                        handlerUI.sendMessage(msg);
                    }
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Erreur read()");
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "TReception run() stop");
        }

        public void arreter()
        {
            if (fini == false)
            {
                fini = true;
            }
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
