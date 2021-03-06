package com.lasalle.io_trucks;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @file MainActivity.java
 * @author Mathieu Arthur
 * @version 0.1
 * @brief Déclaration de la classe MainActivity
 */

/**
 * @class MainActivity
 * @brief Classe IHM principale
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    /**
     * Constantes
     */
    private static final String TAG = "IHMMainActivity";
    /**
     * Attributs
     */
    private Boolean etatTriangle = false;
    private Boolean etatGyrophare = false;
    private Boolean etatEclairage = false;
    private Button buttonBluetooth;
    private Button buttonRechercher;
    private Button buttonRafraichir;
    private ImageButton imageButtonTriangle;
    private ImageButton imageButtonGyrophare;
    private ImageButton imageButtonEclairage;
    private ImageButton imageButtonCharge;
    private TextView textViewEtatCharge;
    private Communication communicationBluetooth = new Communication();
    private Peripherique peripheriqueBluetooth = null;
    private ImageView imageEtatConnection;
    private TextView textEtatConnection;
    private EditText editNomPeripherique;

    /**
     * @brief Méthode appelée à la création de l'activité MainActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate()");

        recupererWidgets();
        initialiserWidgets();

        communicationBluetooth.demanderActivationBluetooth(this);
    }

    /**
     * @brief Méthode appelée au démarrage après le onCreate() ou un restart après un onStop()
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG,"onStart()");
    }

    /**
     * @brief Méthode appelée après onStart() ou après onPause()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG,"onResume()");
        creerLiasonReceiverEtatBluetooth();
    }

    /**
     * @brief Méthode appelée après qu'une boîte de dialogue s'est affichée (on reprend sur un onResume()) ou avant onStop() (activité plus visible)
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG,"onPause()");
        communicationBluetooth.unregisterBluetooth(this);
    }

    /**
     * @brief Méthode appelée lorsque l'activité n'est plus visible
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(TAG,"onStop()");

    }

    /**
     * @brief Méthode appelée à la destruction de l'application (après onStop() et détruite par le système Android)
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG,"onDestroy()");

    }

    /**
     * @brief Méthode de gestion des clics
     * Ceci est la méthode qui gère l'écoute des clics sur les différents widgets de l'interface
     * @param element element définis le widget sur lequel le clic est recenssé
     */
    @Override
    public void onClick(View element)
    {
        if(element == buttonBluetooth)
        {
            if(buttonBluetooth.getText().equals("Connecter"))
            {
                BluetoothDevice blueDevice = communicationBluetooth.recupererAppareilBluetooth(editNomPeripherique.getText().toString());
                if(blueDevice == null)
                {
                    AlertDialog.Builder boiteAvertissementNonTrouver = new AlertDialog.Builder(this);
                    boiteAvertissementNonTrouver.setMessage("L'appareil io-trucks n'as pas été trouvé. Vérifiez si celui a été appairé correctement.");
                    boiteAvertissementNonTrouver.setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    boiteAvertissementNonTrouver.show();
                    Log.i(TAG, "Appareil io-trucks non trouvé !");
                    return;
                }
                else
                {
                    if(peripheriqueBluetooth == null)
                    {
                        Log.i(TAG, "Instancie peripheriqueBluetooth");
                        peripheriqueBluetooth = new Peripherique(blueDevice, handler);
                    }
                    if(!peripheriqueBluetooth.estConnecte())
                    {
                        Log.i(TAG, "Connexion peripheriqueBluetooth");
                        peripheriqueBluetooth.connecter();
                    }
                    else // déjà connecté !
                    {
                    }
                }
            }
            else if(buttonBluetooth.getText().equals("Déconnecter"))
            {
                if (peripheriqueBluetooth.estConnecte())
                {
                    peripheriqueBluetooth.deconnecter();
                }
            }
        }
        else if(element == buttonRechercher)
        {
            communicationBluetooth.rechercherAppareilConnu(this);
        }
        else if(element == imageButtonTriangle)
        {
            Log.i(TAG,"button Triangle");
            if(!etatTriangle)
            {
                envoyerTrame("$iotruck;T;1\r\n");
                imageButtonTriangle.setImageResource(R.drawable.triangle);
                etatTriangle = true;
            }
            else
            {
                envoyerTrame("$iotruck;T;0\r\n");
                imageButtonTriangle.setImageResource(R.drawable.triangle_b_w);
                etatTriangle = false;
            }
        }
        else if(element == imageButtonGyrophare)
        {
            Log.i(TAG,"button Gyrophare");
            if(!etatGyrophare)
            {
                envoyerTrame("$iotruck;G;1\r\n");
                imageButtonGyrophare.setImageResource(R.drawable.flash);
                etatGyrophare = true;
            }
            else
            {
                envoyerTrame("$iotruck;G;0\r\n");
                imageButtonGyrophare.setImageResource(R.drawable.flash_b_w);
                etatGyrophare = false;
            }
        }
        else if(element == imageButtonEclairage)
        {
            Log.i(TAG,"button Eclairage");
            if(!etatEclairage)
            {
                envoyerTrame("$iotruck;E;1\r\n");
                imageButtonEclairage.setImageResource(R.drawable.spotlight);
                etatEclairage = true;
            }
            else
            {
                envoyerTrame("$iotruck;E;0\r\n");
                imageButtonEclairage.setImageResource(R.drawable.spotlight_b_w);
                etatEclairage = false;
            }
        }
        else if(element == buttonRafraichir)
        {
            Log.i(TAG,"button Rafraichir");
            demanderEtats();
        }
        else
        {
            Log.i(TAG,"button Inconnu : " + element.getId());
        }
    }

    /**
     * @brief Méthode qui envoie une trame au périphérique Bluetooth
     */
        private void envoyerTrame(String trame)
    {
        if(peripheriqueBluetooth != null)
        {
            if (peripheriqueBluetooth.estConnecte())
            {
                Log.i(TAG, "envoyerTrame() trame : " + trame);
                peripheriqueBluetooth.envoyer(trame);
            }
        }
    }

    /**
     * @brief Méthode pour associer la vue à l'objet des Widgets
     */
    private void recupererWidgets()
    {
        buttonBluetooth = findViewById(R.id.buttonConnecter);
        buttonRechercher = findViewById(R.id.buttonBounded);
        buttonRafraichir = findViewById(R.id.buttonRafraichir);
        imageButtonTriangle = findViewById(R.id.imageButtonTriangle);
        imageButtonGyrophare = findViewById(R.id.imageButtonGyrophares);
        imageButtonEclairage = findViewById(R.id.imageButtonEclairage);
        imageButtonCharge = findViewById(R.id.imageButtonCharge);
        textViewEtatCharge = findViewById(R.id.textViewEtatCharge);
        imageEtatConnection = findViewById(R.id.imageViewEtatConnection);
        textEtatConnection = findViewById(R.id.textViewEtatConnection);
        editNomPeripherique = findViewById(R.id.editNomPeripherique);;
    }

    /**
     * @brief Méthode pour initialiser les Widgets
     */
    private void initialiserWidgets()
    {
        renitialiserVue();

        buttonBluetooth.setOnClickListener(this);
        buttonRechercher.setOnClickListener(this);
        buttonRafraichir.setOnClickListener(this);
        imageButtonTriangle.setOnClickListener(this);
        imageButtonGyrophare.setOnClickListener(this);
        imageButtonEclairage.setOnClickListener(this);
        buttonBluetooth.setFocusableInTouchMode(true);
        buttonBluetooth.requestFocus();
    }

    /**
     * @brief Méthode pour créer les Registers de l'état du bluetooth et donc le lien avec l'état du bluetooth
     */
    private void creerLiasonReceiverEtatBluetooth()
    {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(communicationBluetooth.ecouterEtatBluetooth(), filter);
    }

    private void activerVue()
    {
        buttonBluetooth.setText("Déconnecter");
        buttonRafraichir.setEnabled(true);
        imageButtonTriangle.setEnabled(true);
        imageButtonGyrophare.setEnabled(true);
        imageButtonEclairage.setEnabled(true);
        //imageButtonCharge.setEnabled(true);
        textViewEtatCharge.setText("Charge");
        imageEtatConnection.setImageResource(R.drawable.green_cricle);
        textEtatConnection.setText("Connecté");
    }

    /**
     * @brief Méthode permettant de rénitialiser la vue de l'activitée
     */
    private void renitialiserVue()
    {
        buttonBluetooth.setText("Connecter");
        buttonRafraichir.setEnabled(false);
        imageButtonTriangle.setEnabled(false);
        imageButtonGyrophare.setEnabled(false);
        imageButtonEclairage.setEnabled(false);
        imageButtonCharge.setEnabled(false);
        textViewEtatCharge.setText("Charge");
        imageButtonTriangle.setImageResource(R.drawable.triangle_b_w);
        imageButtonEclairage.setImageResource(R.drawable.spotlight_b_w);
        imageButtonGyrophare.setImageResource(R.drawable.flash_b_w);
        imageButtonCharge.setImageResource(R.drawable.risk_b_w);
        imageEtatConnection.setImageResource(R.drawable.red_circle);
        textEtatConnection.setText("Déconnecté");
    }

    /**
     * @brief Méthode perméttant de décoder les trames reçues
     * @param trame Contient la trame reçue
     */
    private void decoderTrame(String trame)
    {
        String nouvelleTrame = "";
        // Exemple : trame = "$iotruck;S1;0;0;0\r\n"
        nouvelleTrame = trame.replace(Protocole.EN_TETE,""); // enlever aussi le ; ?
        // Exemple : nouvelleTrame = ";S1;0;0;0\r\n"
        nouvelleTrame.replace(Protocole.DELIMITEUR_FIN,"");
        // Exemple : nouvelleTrame = ";S1;0;0;0"
        String[] trameCouper = nouvelleTrame.split(Protocole.DELIMITEUR_CHAMP);
        // Exemple : trameCouper = [0];[1];[2];[3];[4]
        Log.v(TAG, "decoderTrame() découpage de la trame");
        // le premier champ est vide
        for(int i = 1; i < trameCouper.length; i++)
        {
            Log.v(TAG, "decoderTrame() champ " + i + " = " + trameCouper[i]);
        }
        traiterTrame(trameCouper);
    }

    /**
     * @brief Méthode permettant de traiter les trames en fonctions de leurs contennue
     * @param trame contient la trame reçu après avoir était décoder et découper
     */
    private void traiterTrame(String[] trame)
    {
        Log.v(TAG, "traiterTrame() trame[1] = " + trame[1] + " (type)");
        if(trame[1].equals(Protocole.TRAME_REQUETE_STATE1))
        {
            afficherEtatS1(trame);
        }
        else if(trame[1].equals(Protocole.TRAME_REQUETE_STATE2))
        {
            afficherEtatS2(trame);
        }
        /**
         * @todo Traiter les autres types de trames
         */
    }

    private void afficherEtatS1(String[] trame)
    {
        Log.v(TAG, "afficherEtatS1() trame[2] = " + trame[2] + " (triangle)");
        if(trame[2].equals(Protocole.LEVE))
        {
            imageButtonTriangle.setImageResource(R.drawable.triangle);
            etatTriangle = true;
        }
        else
        {
            imageButtonTriangle.setImageResource(R.drawable.triangle_b_w);
            etatTriangle = false;
        }

        Log.v(TAG, "afficherEtatS1() trame[3] = " + trame[3] + " (gyrophare)");
        if(trame[3].equals(Protocole.ON))
        {
            imageButtonGyrophare.setImageResource(R.drawable.flash);
            etatGyrophare = true;
        }
        else
        {
            imageButtonGyrophare.setImageResource(R.drawable.flash_b_w);
            etatGyrophare = false;
        }

        Log.v(TAG, "afficherEtatS1() trame[4] = " + trame[4] + " (éclairage)");
        if(trame[4].equals(Protocole.ON))
        {
            imageButtonEclairage.setImageResource(R.drawable.spotlight);
            etatEclairage = true;
        }
        else
        {
            imageButtonEclairage.setImageResource(R.drawable.spotlight_b_w);
            etatEclairage = false;
        }
    }

    private void afficherEtatS2(String[] trame)
    {
        Log.v(TAG, "afficherEtatS2() trame[2] = " + trame[2] + " (hayon)");
        Log.v(TAG, "afficherEtatS2() trame[3] = " + trame[3] + " (charge)");
        if(trame[3].equals(Protocole.CHARGE_NORMAL))
        {
            imageButtonCharge.setImageResource(R.drawable.risk_green);
            textViewEtatCharge.setText("Charge normale");
        }
        else if(trame[3].equals(Protocole.CHARGE_ATTENTION))
        {
            imageButtonCharge.setImageResource(R.drawable.risk_yellow);
            textViewEtatCharge.setText("Charge élevée");
        }
        else if(trame[3].equals(Protocole.CHARGE_SURCHARGE))
        {
            imageButtonCharge.setImageResource(R.drawable.risk_red);
            textViewEtatCharge.setText("Surcharge");
        }
    }

    /**
     * @brief Handler de l'application et des périphériques bluetooth
     * Cette handler permet de gérer le thread de communication de l'application
     */
    final private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch(msg.what)
            {
                case Peripherique.CODE_CONNEXION:
                    Log.v(TAG, "handleMessage() io-trucks connecté");
                    activerVue();
                    demanderEtats();
                    break;
                case Peripherique.CODE_RECEPTION:
                    Log.v(TAG, "handleMessage() io-trucks réception : " + (String)msg.obj);
                    decoderTrame((String)msg.obj);
                    break;
                case Peripherique.CODE_EMISSION:
                    Log.v(TAG, "handleMessage() io-trucks émission : " + (String)msg.obj);
                    /**
                     * @todo Voir si on a besoin de faire quelque chose une fois la trame envoyée
                     */
                    break;
                case Peripherique.CODE_DECONNEXION:
                    Log.v(TAG, "handleMessage() io-trucks déconnecté");
                    renitialiserVue();
                    break;
            }
        }
    };

    /**
     * @detail Méthode qui envoie les trames de demande d'états S1 et S2
     */
    private void demanderEtats()
    {
        envoyerTrame("$iotruck;S1\r\n");
        envoyerTrame("$iotruck;S2\r\n");
    }
}
