package com.lasalle.io_trucks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * @file Accueil.java
 * @author Mathieu Arthur
 * @version 0.1
 * @brief Déclaration de la classe Accueil
 */

/**
 * @class Accueil
 * @brief Classe de l'activité Accueil
 * @detail La classe Acceuil est l'activité de démarrage de l'application.
 */
public class Accueil extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG = "IHMAccueil";
    /**
     * Attributs
     */
    private Animation animation;
    private ImageView imageView;

    /**
     * @brief Méthode appelée à la création de l'activité Accueil
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);
        Log.i(TAG,"onCreate()");

        imageView = (ImageView)findViewById(R.id.imageView);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            /**
             * @brief Méthode perméttant la gestion du démarrage de l'animation
             * Cette méthode définit ce qui se passe au démarrage de l'animation
             * @param animation Animation vers l'activité suivante
             */
            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            /**
             * @brief Méthode perméttant la gestion de la fin de l'aniamtion
             * Cette méthode définit ce qui se passe à la fin de l'animation
             * Ici, elle démarre l'activité : MainActivity
             * @param animation Animation vers l'activité suivante
             */
            @Override
            public void onAnimationEnd(Animation animation)
            {
                // A la fin de l'animation, on lance l'activité principale
                Intent intent = new Intent(Accueil.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }
        });
        imageView.startAnimation(animation);
    }
}
