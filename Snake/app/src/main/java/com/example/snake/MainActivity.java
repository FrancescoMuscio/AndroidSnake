package com.example.snake;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        snakeView = new SnakeView(this);
        setContentView(snakeView);

        snakeView.setOnPauseListener(() -> mostraDialogPausa());
    }

    private void mostraDialogPausa() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pausa");

        // Inflate layout
        final android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_pause, null);
        builder.setView(dialogView);

        final SeekBar seekBarSpeed = dialogView.findViewById(R.id.seekBarSpeed);
        final TextView textSpeedValue = dialogView.findViewById(R.id.textSpeedValue);

        seekBarSpeed.setProgress(snakeView.getVelocita());
        textSpeedValue.setText("Velocità: " + snakeView.getVelocita());

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSpeedValue.setText("Velocità: " + progress);
                snakeView.setVelocita(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        builder.setPositiveButton("Riprendi", (dialog, which) -> {
            snakeView.resumeGame();
            dialog.dismiss();
        });

        builder.setNegativeButton("Esci", (dialog, which) -> {
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }
}

