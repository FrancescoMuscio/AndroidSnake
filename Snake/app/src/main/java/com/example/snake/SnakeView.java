package com.example.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class SnakeView extends View {

    private static final int NUM_CELLE = 20;
    private int larghezzaCella, altezzaCella;

    private ArrayList<Point> serpente;
    private Point cibo;

    private Paint paintSerpente, paintCibo, paintTesto;

    public enum Direzione {SU, GIU, SINISTRA, DESTRA}
    private Direzione direzione = Direzione.DESTRA;

    private Handler handler = new Handler();

    private final int DELAY_MIN = 50;
    private final int DELAY_MAX = 500;

    private int velocita = 5; // da 0 (lento) a 10 (veloce)
    private int delay;

    private Random random = new Random();

    private int punteggio = 0;
    private boolean inPausa = false;

    private float touchStartX, touchStartY;
    private final int SWIPE_THRESHOLD = 100;

    private OnPauseListener onPauseListener;

    public interface OnPauseListener {
        void onPause();
    }

    public SnakeView(Context context) {
        super(context);

        setBackgroundColor(Color.WHITE);

        paintSerpente = new Paint();
        paintSerpente.setColor(Color.GREEN);

        paintCibo = new Paint();
        paintCibo.setColor(Color.RED);

        paintTesto = new Paint();
        paintTesto.setColor(Color.BLACK);
        paintTesto.setTextSize(60);
        paintTesto.setAntiAlias(true);

        setVelocita(velocita);
        initGame();

        handler.postDelayed(runnable, delay);
    }

    public void setOnPauseListener(OnPauseListener listener) {
        this.onPauseListener = listener;
    }

    public int getVelocita() {
        return velocita;
    }

    public void setVelocita(int v) {
        if (v < 0) v = 0;
        if (v > 10) v = 10;
        velocita = v;
        delay = DELAY_MAX - (velocita * (DELAY_MAX - DELAY_MIN) / 10);
    }

    public void pauseGame() {
        inPausa = true;
        invalidate();
    }

    public void resumeGame() {
        inPausa = false;
        invalidate();
    }

    private void initGame() {
        serpente = new ArrayList<>();
        serpente.add(new Point(5, 10));
        serpente.add(new Point(4, 10));
        serpente.add(new Point(3, 10));

        direzione = Direzione.DESTRA;
        punteggio = 0;
        inPausa = false;

        generaCibo();
    }

    private void generaCibo() {
        while (true) {
            int x = random.nextInt(NUM_CELLE);
            int y = random.nextInt(NUM_CELLE);
            Point nuovoCibo = new Point(x, y);
            if (!serpente.contains(nuovoCibo)) {
                cibo = nuovoCibo;
                break;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        larghezzaCella = w / NUM_CELLE;
        altezzaCella = h / NUM_CELLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        // Griglia rimossa - non disegniamo linee

        // Disegna serpente
        for (Point p : serpente) {
            canvas.drawRect(p.x * larghezzaCella,
                    p.y * altezzaCella,
                    (p.x + 1) * larghezzaCella,
                    (p.y + 1) * altezzaCella,
                    paintSerpente);
        }

        // Disegna cibo
        canvas.drawRect(cibo.x * larghezzaCella,
                cibo.y * altezzaCella,
                (cibo.x + 1) * larghezzaCella,
                (cibo.y + 1) * altezzaCella,
                paintCibo);

        // Disegna punteggio
        canvas.drawText("Punteggio: " + punteggio, 10, getHeight() - 20, paintTesto);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!inPausa) {
                aggiorna();
                invalidate();
            }
            handler.postDelayed(this, delay);
        }
    };

    private void aggiorna() {
        Point testa = serpente.get(0);
        Point nuovaTesta = new Point(testa.x, testa.y);

        switch (direzione) {
            case SU:
                nuovaTesta.y--;
                break;
            case GIU:
                nuovaTesta.y++;
                break;
            case SINISTRA:
                nuovaTesta.x--;
                break;
            case DESTRA:
                nuovaTesta.x++;
                break;
        }

        // Controllo collisioni con i muri
        if (nuovaTesta.x < 0 || nuovaTesta.x >= NUM_CELLE ||
                nuovaTesta.y < 0 || nuovaTesta.y >= NUM_CELLE) {
            resetGame();
            return;
        }

        // Controlla collisione con se stesso (esclude coda che verrà rimossa)
        ArrayList<Point> corpoSenzaCoda = new ArrayList<>(serpente);
        corpoSenzaCoda.remove(corpoSenzaCoda.size() - 1);

        if (corpoSenzaCoda.contains(nuovaTesta)) {
            resetGame();
            return;
        }

        serpente.add(0, nuovaTesta);

        if (nuovaTesta.equals(cibo)) {
            generaCibo();
            punteggio += 10;
        } else {
            serpente.remove(serpente.size() - 1);
        }
    }

    private void resetGame() {
        punteggio = 0;
        direzione = Direzione.DESTRA;
        inPausa = false;
        initGame();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - touchStartX;
                float deltaY = event.getY() - touchStartY;

                // Tap breve = pausa
                if (Math.abs(deltaX) < 20 && Math.abs(deltaY) < 20) {
                    pauseGame();
                    if (onPauseListener != null) {
                        onPauseListener.onPause();
                    }
                    return true;
                }

                // Swipe per cambiare direzione
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (deltaX > 0 && direzione != Direzione.SINISTRA) {
                            direzione = Direzione.DESTRA;
                        } else if (deltaX < 0 && direzione != Direzione.DESTRA) {
                            direzione = Direzione.SINISTRA;
                        }
                    }
                } else {
                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY > 0 && direzione != Direzione.SU) {
                            direzione = Direzione.GIU;
                        } else if (deltaY < 0 && direzione != Direzione.GIU) {
                            direzione = Direzione.SU;
                        }
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return this.x == p.x && this.y == p.y;
        }

        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }
}



