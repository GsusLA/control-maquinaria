package mx.grupohi.almacenes.controlmaquinaria;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Obra;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

import static android.Manifest.permission.READ_PHONE_STATE;

public class LoginActivity extends AppCompatActivity {
    Button iniciar;
    // UI references.
    private AutoCompleteTextView mUsuarioView;
    private TextInputLayout formLayout;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private UserLoginTask mAuthTask = null;
    private ArrayList<Obra> obrasActuales;
    private Usuario usuario;
    Camera2BasicFragment fotos;

    private Login login;

    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fotos = Camera2BasicFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fotos)
                .commit();


        login = new Login(this);

        iniciar = (Button)findViewById(R.id.btn_loginIniciar);
        mUsuarioView = (AutoCompleteTextView) findViewById(R.id.usuario);
        mPasswordView = (EditText) findViewById(R.id.password);
        formLayout = (TextInputLayout) findViewById(R.id.layout);

        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isNetworkStatusAvialable(getApplicationContext())) {
                    fotos.tomarFoto();
                    attemptLogin();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();
                }
                //startActivity(new Intent(LoginActivity.this, ObraActivity.class));
            }
        });

        mUsuarioView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.requestFocus();
                }
                return false;
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    iniciar.performClick();
                }
                return false;
            }
        });

        if(ContextCompat.checkSelfPermission(LoginActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 100);

        }
    }

    /**
     *  Hace las validaciones iniciales al momento de logearse en la aplicación
     */
    private void attemptLogin() {

        // Resetear errores.
        mUsuarioView.setError(null);
        mPasswordView.setError(null);
        formLayout.setError(null);

        // Store values at the time of the login attempt.
        final String usuario = mUsuarioView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(usuario)) {
            mUsuarioView.setError(getString(R.string.error_field_required));
            focusView = mUsuarioView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mProgressDialog = ProgressDialog.show(LoginActivity.this, "Autenticando", "Por favor espere...", true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mAuthTask = new UserLoginTask(usuario, password);
                    mAuthTask.execute((Void) null);
                }
            }).run();
        }
    }

    /**
     *  Valida que activity es el siguiente, revisa si ya hay una obra sincronizada, de lo contrario iniciara el activity de Obra
     *  para sincronizar los datos de la que se desee trabajar.
     */
    private void nextActivity(){
        if(login.validarObra()){
            new SincronizacionObra(getApplicationContext()).guardarToken(token);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));

        }else{
            Intent intent = new Intent(LoginActivity.this, ObraActivity.class);
            intent.putExtra("Obras", obrasActuales);
            intent.putExtra("Usuario", usuario);
            intent.putExtra("token", token);
            startActivity(intent);
        }
    }

    /**
     * Tarea asincrona para el login y la sincronizacion de usuario y de obras
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String user;
        private final String pass;

        UserLoginTask(String email, String password) {
            user = email;
            pass = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ContentValues data = new ContentValues();
            data.put("usuario", user);
            data.put("clave", pass);

            try {
                Thread.sleep(5000);
                URL url = new URL(getApplicationContext().getString(R.string.url_login));

                final JSONObject JSON = HttpConnection.POST(url, data);

                if(JSON.toString().contains("invalid_credentials")){
                    errorMessage(getResources().getString(R.string.error_sesion_datos));
                    return false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setTitle("Actualizando");
                        mProgressDialog.setMessage("Actualizando datos de usuario...");
                    }
                });


                JSONObject user = JSON.getJSONObject("user");
                token = JSON.getString("token");

                usuario = new Usuario();
                usuario.setDescripcion(user.getString("nombre") + " " + user.getString("apaterno"));
                usuario.setNombre_usuario(user.getString("usuario"));
                usuario.setIdusuario(user.getInt("idusuario"));
                usuario.setClave(pass);
                usuario.setImei(Util.deviceImei(getApplicationContext()));

                //manejar guardado de datos en la DB
                if(!login.guardarUsuario(usuario)){
                    errorMessage(getResources().getString(R.string.error_guardar_db));
                    return false;
                }
                //Las obras recuperadas de mandaran al activity de sincronizacion para que se seleccione con la que se va a trabajar
                obrasActuales = new ArrayList<>();
                Obra obra;
                final JSONArray obras = JSON.getJSONArray("obras");
                for (int i = 0; i<obras.length();i++){
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.setMessage("Actualizando catálogo de Obras... \n Obra " + (finalI +1) + " de " + obras.length());
                        }
                    });

                    JSONObject infoObra = obras.getJSONObject(i);

                    obra = new Obra();
                    obra.setIdObra(infoObra.getInt("id_obra"));
                    obra.setDescripcion(infoObra.getString("descripcion"));
                    obra.setBase(infoObra.getString("databaseName"));
                    obra.setNombre(infoObra.getString("nombre"));
                    obra.setToken(token);

                    obrasActuales.add(obra);
                }

            }catch (Exception e){
                e.printStackTrace();
                errorMessage(getResources().getString(R.string.general_exception));
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mAuthTask = null;
            mProgressDialog.dismiss();
            if (aBoolean) {
                mUsuarioView.setText("");
                mPasswordView.setText("");

                nextActivity();
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void errorMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
