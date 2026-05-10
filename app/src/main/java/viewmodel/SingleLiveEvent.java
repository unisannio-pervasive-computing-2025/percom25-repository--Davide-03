package viewmodel;

import android.util.Log;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Un osservabile sensibile al ciclo di vita che invia aggiornamenti solo dopo la sottoscrizione.
 * Viene utilizzato per eventi "una tantum" come la navigazione o i messaggi Toast.
 * 
 * Risolve il problema comune in cui gli eventi vengono riemessi durante un cambio
 * di configurazione (es. rotazione dello schermo). Questo LiveData chiama l'osservatore
 * solo se c'è stata una chiamata esplicita a setValue() o postValue().
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private static final String TAG = "SingleLiveEvent";

    // AtomicBoolean garantisce che il valore venga consumato una sola volta in modo sicuro
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Sono stati registrati più osservatori, ma solo uno riceverà le notifiche dei cambiamenti.");
        }

        // Osserva il MutableLiveData interno
        super.observe(owner, t -> {
            // Se c'è un evento in sospeso, lo consumiamo e notifichiamo l'osservatore
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    @Override
    public void setValue(@Nullable T t) {
        // Segnala che c'è un nuovo valore da consumare
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Comodo per eventi senza dati (void).
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}
