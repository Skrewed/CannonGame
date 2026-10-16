// MainActivityFragment.java
// MainActivityFragment cria e gerencia um CannonView (Capítulo 6, Seção 6.7)
package com.deitel.cannongame;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment responsável por inflar o layout fragment_main.xml, configurar o
 * stream de áudio do dispositivo e gerenciar o ciclo de vida do CannonView.
 */
public class MainActivityFragment extends Fragment {

    private CannonView cannonView; // view customizada para exibir o jogo

    // Chamado quando a view do Fragment precisa ser criada (Fig. 6.6)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Infla o layout fragment_main.xml
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Obtém uma referência para o CannonView
        cannonView = view.findViewById(R.id.cannonView);
        return view;
    }

    // Configura o controle de volume assim que a Activity/View é criada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null) {
            // Permite que os botões de volume controlem o som do jogo (Seção 6.7)
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    // Quando a MainActivity entra em pausa, interrompe o jogo (Fig. 6.6)
    @Override
    public void onPause() {
        super.onPause();
        if (cannonView != null) {
            cannonView.stopGame(); // encerra o loop do jogo
        }
    }

    // Quando a MainActivity é destruída, o MainActivityFragment libera os recursos (Fig. 6.6)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cannonView != null) {
            cannonView.releaseResources();
        }
    }
}
