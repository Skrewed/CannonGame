// MainActivity.java
// MainActivity exibe o MainActivityFragment (Capítulo 6, Seção 6.6)
package com.deitel.cannongame;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity principal que hospeda o MainActivityFragment em modo paisagem e tela cheia.
 */
public class MainActivity extends AppCompatActivity {

    // Chamado quando o aplicativo é iniciado pela primeira vez (Fig. 6.5)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
