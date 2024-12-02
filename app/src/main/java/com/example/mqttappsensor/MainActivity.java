package com.example.mqttappsensor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private EditText edtNombre, edtEdad, edtNacionalidad;
    private RadioGroup radioSexo, radioEstadoCivil;
    private Button btnEnviar;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // Conexión MQTT
    private static final String MQTT_BROKER = "tcp://mqttservidoriots.cloud.shiftr.io:1883"; // URL MQTT
    private static final String MQTT_TOPIC = "usuario/datos";
    private static final String MQTT_USER = "mqttservidoriots";
    private static final String MQTT_PASSWORD = "DaMDl3xTHRhT4Q2M"; // Token

    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializo Firebase
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("usuarios");

        // Inicializo vistas
        edtNombre = findViewById(R.id.edtNombre);
        edtEdad = findViewById(R.id.edtEdad);
        edtNacionalidad = findViewById(R.id.edtNacionalidad);
        radioSexo = findViewById(R.id.radioSexo);
        radioEstadoCivil = findViewById(R.id.radioEstadoCivil);
        btnEnviar = findViewById(R.id.btnEnviar);

        // Inicializo MQTT
        try {
            mqttClient = new MqttClient(MQTT_BROKER, MqttClient.generateClientId(), null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MQTT_USER);
            options.setPassword(MQTT_PASSWORD.toCharArray());
            mqttClient.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al conectar con MQTT", Toast.LENGTH_SHORT).show();
        }

        // Boton de enviar los datos ;)
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = edtNombre.getText().toString();
                String edad = edtEdad.getText().toString();
                String nacionalidad = edtNacionalidad.getText().toString();
                int sexoId = radioSexo.getCheckedRadioButtonId();
                int estadoCivilId = radioEstadoCivil.getCheckedRadioButtonId();

                // Valido aqui los campos
                if (nombre.isEmpty() || edad.isEmpty() || nacionalidad.isEmpty() ||
                        sexoId == -1 || estadoCivilId == -1) {  // Verifico si no se seleccionó un radioButton
                    Toast.makeText(MainActivity.this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Obtengo los valores de los RadioButtons seleccionados
                    String sexo = ((RadioButton) findViewById(sexoId)).getText().toString();
                    String estadoCivil = ((RadioButton) findViewById(estadoCivilId)).getText().toString();

                    // Envio los datos a FIRE
                    String userId = myRef.push().getKey();
                    if (userId != null) {
                        myRef.child(userId).setValue(new Usuario(nombre, edad, sexo, nacionalidad, estadoCivil));

                        // Envio los datos a MQTT
                        try {
                            String message = "Nombre: " + nombre + ", Edad: " + edad + ", Sexo: " + sexo + ", Nacionalidad: " + nacionalidad + ", Estado Civil: " + estadoCivil;
                            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                            mqttMessage.setQos(1);
                            mqttClient.publish(MQTT_TOPIC, mqttMessage);

                            Toast.makeText(MainActivity.this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show();
                        } catch (MqttException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al enviar los datos a MQTT", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

