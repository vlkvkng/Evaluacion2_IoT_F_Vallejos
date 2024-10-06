package com.example.ev2_fv;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText nombreEditText, rutEditText, incidenteEditText;
    private TextView fechaHoraTextView;
    private Spinner laboratorioSpinner;
    private SensorManager sensorManager;
    private boolean Vertical = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        nombreEditText = findViewById(R.id.nombre);
        rutEditText = findViewById(R.id.rut);
        incidenteEditText = findViewById(R.id.incidente);
        laboratorioSpinner = findViewById(R.id.elegirLaboratorio);
        fechaHoraTextView = findViewById(R.id.fechaHora);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        Button btnGrabar = findViewById(R.id.btnGrabar);
        btnGrabar.setOnClickListener(v -> {
            grabarIncidenteConConfirmacion();
        });


        startUpdatingTime();
    }

    private void startUpdatingTime() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                actualizarHora();
                handler.postDelayed(this, 1000);
            }
        });
    }


    private void actualizarHora() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        fechaHoraTextView.setText(currentDateAndTime);
    }


    private void mostrarConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("¿Está seguro de que desea grabar el incidente?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    grabarIncidente();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void grabarIncidenteConConfirmacion() {
        if (validarCampos()) {
            mostrarConfirmacion();}
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float y = event.values[1];


            if (Math.abs(y) > 9) {
                if (!Vertical) {
                    Vertical = true;

                    if (validarCampos()) {
                        mostrarConfirmacion();
                    }
                }
            } else {
                Vertical = false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    private boolean validarCampos() {
        String nombre = nombreEditText.getText().toString().trim();
        String rut = rutEditText.getText().toString().trim();
        String incidente = incidenteEditText.getText().toString().trim();
        String laboratorioSeleccionado = laboratorioSpinner.getSelectedItem().toString();


        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(rut) || TextUtils.isEmpty(incidente) || laboratorioSeleccionado.equals("Seleccione laboratorio")) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (!validarRUT(rut)) {
            Toast.makeText(this, "RUT no válido, ingrese uno válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void grabarIncidente() {
        Toast.makeText(this, "Datos Grabados", Toast.LENGTH_SHORT).show();
    }


    public boolean validarRUT(String rut) {
        String ElRut = rut.replace(".", "").replace("-", "").toUpperCase();

        if (ElRut.length() < 8) {
            return false;
        }

        String rutNumero = ElRut.substring(0, ElRut.length() - 1);
        char dvIngresado = ElRut.charAt(ElRut.length() - 1);

        char dvCalculado = Digitoverificador(rutNumero);

        return dvIngresado == dvCalculado;
    }


    private char Digitoverificador(String rutNumero) {
        int suma = 0;
        int multiplicador = 2;

        for (int i = rutNumero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(rutNumero.charAt(i)) * multiplicador;
            multiplicador = (multiplicador < 7) ? multiplicador + 1 : 2;
        }

        int rd = 11 - (suma % 11);

        switch (rd) {
            case 11:
                return '0';
            case 10:
                return 'K';
            default:
                return (char) (rd + '0');
        }
    }
}
