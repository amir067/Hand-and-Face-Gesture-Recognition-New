/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.mcs.FaceGestureApp.ui.hand.MlKit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mcs.FaceGestureApp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Fragmentos b??sicos para la c??mara. */
public class Camera2BasicFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

  /** Tag for the {@link Log}. */
  private static final String TAG = "Hand gesture";

  private static final String FRAGMENT_DIALOG = "dialog";

  private static final String HANDLE_THREAD_NAME = "CameraBackground";

  private static final int PERMISSIONS_REQUEST_CODE = 1;

  private final Object lock = new Object();
  private boolean runClassifier = false;
  private boolean checkedPermissions = false;
  private TextView textView, textViewGesture;
  private ImageClassifier classifier;

  /** Ancho m??ximo de vista previa garantizado por Camera2 API */
  private static final int MAX_PREVIEW_WIDTH = 4608;

  /** M??xima altura de previsualizaci??n garantizada por Camera2 API */
  private static final int MAX_PREVIEW_HEIGHT = 2592;

  /**
   * {@link TextureView.SurfaceTextureListener} controla varios eventos de ciclo de {@link
   * TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
          new TextureView.SurfaceTextureListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
              openCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
              configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
              return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }
          };

  /** Identificador de la corriente  {@link CameraDevice}. */
  private String cameraId;

  /** An {@link AutoFitTextureView2} para la vista previa de la c??mara. */
  private AutoFitTextureView2 textureView;

  /** A {@link CameraCaptureSession } para la vista previa de la c??mara. */
  private CameraCaptureSession captureSession;

  /** Una referencia al abierto {@link CameraDevice}. */
  private CameraDevice cameraDevice;

  /** La {@link Size} de la vista previa de la c??mara. */
  private Size previewSize;

  /** {@link CameraDevice.StateCallback} se llama cuando {@link CameraDevice} cambia su estado. */
  private final CameraDevice.StateCallback stateCallback =
          new CameraDevice.StateCallback() {

            @Override
            public void onOpened(@NonNull CameraDevice currentCameraDevice) {
              // Se llama a este m??todo cuando se abre la c??mara.  Empezamos la vista previa de la c??mara.
              cameraOpenCloseLock.release();
              cameraDevice = currentCameraDevice;
              createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
              cameraOpenCloseLock.release();
              currentCameraDevice.close();
              cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
              cameraOpenCloseLock.release();
              currentCameraDevice.close();
              cameraDevice = null;
              Activity activity = getActivity();
              if (null != activity) {
                activity.finish();
              }
            }
          };

  /** Un subproceso adicional para ejecutar tareas que no deber??an bloquear la interfaz de usuario.  */
  private HandlerThread backgroundThread;

  /** A {@link Handler} para ejecutar tareas en segundo plano. */
  private Handler backgroundHandler;

  /** An {@link ImageReader} que controla la captura de im??genes. */
  private ImageReader imageReader;

  /** {@link CaptureRequest.Builder} para la vista previa de la c??mara */
  private CaptureRequest.Builder previewRequestBuilder;

  /** {@link CaptureRequest} generado por {@link #previewRequestBuilder} */
  private CaptureRequest previewRequest;

  /** A {@link Semaphore} para evitar que la aplicaci??n se salga antes de cerrar la c??mara. */
  private Semaphore cameraOpenCloseLock = new Semaphore(1);

  /** A {@link CameraCaptureSession.CaptureCallback} que controla los eventos relacionados con la captura. */
  private CameraCaptureSession.CaptureCallback captureCallback =
          new CameraCaptureSession.CaptureCallback() {

            @Override
            public void onCaptureProgressed(
                    @NonNull CameraCaptureSession session,
                    @NonNull CaptureRequest request,
                    @NonNull CaptureResult partialResult) {
            }

            @Override
            public void onCaptureCompleted(
                    @NonNull CameraCaptureSession session,
                    @NonNull CaptureRequest request,
                    @NonNull TotalCaptureResult result) {
            }
          };

  /**
   * Muestra una{@link Toast} en el subproceso de interfaz de usuario para los resultados de clasificaci??n.
   *
   * @param text El mensaje para mostrar
   */
  private void showToast(final String text) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
              new Runnable() {
                @Override
                public void run() {
                  textView.setText(text);
                  if (String.valueOf(text.charAt(14)).equalsIgnoreCase("(")) {
                    textViewGesture.setText(String.valueOf(text.charAt(15)));
                  } else if (String.valueOf(text.charAt(14)).equalsIgnoreCase(")")) {
                    textViewGesture.setText(String.valueOf(text.charAt(13)));
                  } else {
                    textViewGesture.setText(String.valueOf(text.charAt(14)));
                  }
                }
              });
    }
  }

  /**
   * Redimensiona la imagen.
   *
   * Intentar usar un tama??o de vista previa demasiado grande podr??a exceder la limitaci??n de ancho de banda del bus de la c??mara,
   * resultando en preciosas vistas previas, pero el almacenamiento de datos de captura de basura.
   *
   * Dado {@code choices} de {@code Size}s con el apoyo de una c??mara, elija la m??s peque??a que
   * por lo menos tan grande como el tama??o de la vista de textura respectiva, y que es a lo sumo tan grande como el
   * tama??o m??ximo respectivo, y cuya relaci??n de aspecto coincida con el valor especificado. Si tal tama??o
   * no existe, elija el m??s grande que es a lo sumo tan grande como el tama??o m??ximo respectivo, y
   * cuya relaci??n de aspecto coincida con el valor especificado.
   *
   * @param choices La lista de tama??os que admite la c??mara para la clase de salida prevista
   * @param textureViewWidth El ancho de la vista de textura relativa a la coordenada del sensor
   * @param textureViewHeight La altura de la vista de textura relativa a la coordenada del sensor
   * @param maxWidth El ancho m??ximo que se puede elegir
   * @param maxHeight La altura m??xima que se puede elegir
   * @param aspectRatio La relaci??n de aspecto
   * @return El ??ptimo  {@code Size}, o uno arbitrario si ninguno era lo suficientemente grande
   */
  private static Size chooseOptimalSize(
          Size[] choices,
          int textureViewWidth,
          int textureViewHeight,
          int maxWidth,
          int maxHeight,
          Size aspectRatio) {

    // Recopilar las resoluciones soportadas que son al menos tan grandes como la superficie de vista previa
    List<Size> bigEnough = new ArrayList<>();
    // Recopilar las resoluciones admitidas que son m??s peque??as que la superficie de vista previa
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth
              && option.getHeight() <= maxHeight
              && option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Escoge la m??s peque??a de esas lo suficientemente grandes. Si no hay nadie lo suficientemente grande, escoja el
    // m??s grande de los que no son lo suficientemente grandes.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "No se pudo encontrar ning??n tama??o de vista previa adecuado");
      return choices[0];
    }
  }

  public static Camera2BasicFragment newInstance() {
    return new Camera2BasicFragment();
  }

  /** Dise??o de la vista previa y los botones. */
  @Override
  public View onCreateView(
          LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_camera, container, false);
  }

  /** Conecte los botones a su controlador de eventos. */
  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    textureView = (AutoFitTextureView2) view.findViewById(R.id.texture2);
    textView = (TextView) view.findViewById(R.id.text);
    textViewGesture = (TextView) view.findViewById(R.id.text2);
  }

  /** Cargue el modelo y las etiquetas. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    try {
      classifier = new ImageClassifier(getActivity());
    } catch (IOException e) {
      Log.e(TAG, "Error al inicializar un clasificador de imagen.");
    }
    startBackgroundThread();
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();

    // Cuando la pantalla se apaga y se vuelve a encender, el SurfaceTexture ya est??
    // disponible y  "onSurfaceTextureAvailable " no se llamar??. En ese caso, podemos abrir
    // una c??mara y empezar a previsualizar desde aqu?? (de lo contrario, esperamos hasta que la superficie est?? lista en
    // el SurfaceTextureListener).
    if (textureView.isAvailable()) {
      openCamera(textureView.getWidth(), textureView.getHeight());
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    classifier.close();
    super.onDestroy();
  }

  /**
   * Configura las variables miembro relacionadas con la c??mara.
   *
   * @param width La anchura del tama??o disponible para la vista previa de la c??mara
   * @param height La altura del tama??o disponible para la vista previa de la c??mara
   */
  private void setUpCameraOutputs(int width, int height) {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      for (String cameraId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // No usamos una c??mara frontal en esta muestra.
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        StreamConfigurationMap map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
          continue;
        }

        //Para capturas de im??genes fijas, usamos el tama??o m??s grande disponible.
        Size largest =
                Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
        imageReader =
                ImageReader.newInstance(
                        largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);

        // Averig??e si necesitamos cambiar la dimensi??n para obtener el tama??o de vista previa en relaci??n con el sensoror
        // Coordinar.
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        // noinspection ConstantConditions
        /* Orientaci??n del sensor de la c??mara */
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (displayRotation) {
          case Surface.ROTATION_0:
          case Surface.ROTATION_180:
            if (sensorOrientation == 90 || sensorOrientation == 270) {
              swappedDimensions = true;
            }
            break;
          case Surface.ROTATION_90:
          case Surface.ROTATION_270:
            if (sensorOrientation == 0 || sensorOrientation == 180) {
              swappedDimensions = true;
            }
            break;
          default:
            Log.e(TAG, "La rotaci??n de la pantalla no es v??lida: " + displayRotation);
            Toast.makeText(activity, "La rotaci??n de la pantalla no es v??lida: " + displayRotation, Toast.LENGTH_SHORT).show();
        }

        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (swappedDimensions) {
          rotatedPreviewWidth = height;
          rotatedPreviewHeight = width;
          maxPreviewWidth = displaySize.y;
          maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
          maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
          maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }

        previewSize =
                chooseOptimalSize(
                        map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth,
                        rotatedPreviewHeight,
                        maxPreviewWidth,
                        maxPreviewHeight,
                        largest);

        // Ajustamos la relaci??n de aspecto de TextureView con el tama??o de la vista previa que elegimos.
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
        } else {
          textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }

        this.cameraId = cameraId;
        return;
      }
    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      // Actualmente se produce un Camera2API cuando se utiliza el
      // dispositivo este c??digo se ejecuta.
      //ErrorDialog.newInstance(getString(R.string.camera_error)).show(getFragmentManager(), FRAGMENT_DIALOG);
    }
  }

  private String[] getRequiredPermissions() {
    Activity activity = getActivity();
    try {
      PackageInfo info =
              activity
                      .getPackageManager()
                      .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  /** Abre la c??mara especificada por {@link  Camera2BasicFragment#cameraId}. */
  @RequiresApi(api = Build.VERSION_CODES.M)
  private void openCamera(int width, int height) {
    if (!checkedPermissions && !allPermissionsGranted()) {
//      getTargetFragment().requestPermissions(getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
  //    return;
    } else {
    }
    checkedPermissions = true;

    setUpCameraOutputs(width, height);
    configureTransform(width, height);
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Tiempo de espera para bloquear la apertura de la c??mara.");
      }
      if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return;
      }
      manager.openCamera(cameraId, stateCallback, backgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrumpido al intentar bloquear la apertura de la c??mara.", e);
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (ContextCompat.checkSelfPermission(getActivity(), permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /** Cierra la corriente {@link CameraDevice}. */
  private void closeCamera() {
    try {
      cameraOpenCloseLock.acquire();
      if (null != captureSession) {
        captureSession.close();
        captureSession = null;
      }
      if (null != cameraDevice) {
        cameraDevice.close();
        cameraDevice = null;
      }
      if (null != imageReader) {
        imageReader.close();
        imageReader = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrumpido al intentar cerrar la c??mara.", e);
    } finally {
      cameraOpenCloseLock.release();
    }
  }

  /** Inicia un subproceso de fondo y su {@link Handler}. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
    synchronized (lock) {
      runClassifier = true;
    }
    backgroundHandler.post(periodicClassify);
  }

  /** Detiene el hilo de fondo y su {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
      synchronized (lock) {
        runClassifier = false;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Toma fotos y las clasifica peri??dicamente. */
  private Runnable periodicClassify =
      new Runnable() {
        @Override
        public void run() {
          synchronized (lock) {
            if (runClassifier) {
              classifyFrame();
            }
          }
          backgroundHandler.post(periodicClassify);
        }
      };

  /** Crea una nueva {@link CameraCaptureSession} para la vista previa de la c??mara. */
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = textureView.getSurfaceTexture();
      assert texture != null;

      // Configuramos el tama??o del b??fer predeterminado para que sea el tama??o de la vista previa de la c??mara que queramos.
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

      // Esta es la superficie de salida que necesitamos para iniciar la vista previa.
      Surface surface = new Surface(texture);

      // Hemos creado un CaptureRequest. Builder con la superficie de salida.
      previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      previewRequestBuilder.addTarget(surface);

      // Aqu??, creamos un CameraCaptureSession para la vista previa de la c??mara.
      cameraDevice.createCaptureSession(
          Arrays.asList(surface),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // La c??mara ya est?? cerrada
              if (null == cameraDevice) {
                return;
              }

              // Cuando la sesi??n est?? lista, empezaremos a mostrar la vista previa.
              captureSession = cameraCaptureSession;
              try {
                // El enfoque autom??tico debe ser continuo para la vista previa de la c??mara.
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finalmente, empezamos a mostrar la vista previa de la c??mara.
                previewRequest = previewRequestBuilder.build();
                captureSession.setRepeatingRequest(
                    previewRequest, captureCallback, backgroundHandler);
              } catch (CameraAccessException e) {
                e.printStackTrace();
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              showToast("Fallido");
            }
          },
          null);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Configura lo necesario {@link Matrix} transformaci??n a ' textureView '. Este
   * el m??todo se debe llamar despu??s de que el tama??o de la vista previa de la c??mara se determina en setUpCameraOutputs y
   * tambi??n el tama??o de ' textureView ' es fijo.
   *
   * @param viewWidth El ancho de ' textureView '
   * @param viewHeight La altura de ' textureView '
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == textureView || null == previewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale =
          Math.max(
              (float) viewHeight / previewSize.getHeight(),
              (float) viewWidth / previewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
    textureView.setTransform(matrix);
  }

  /** Clasifica un fotograma de la secuencia de vista previa. */
  private void classifyFrame() {
    if (classifier == null || getActivity() == null || cameraDevice == null) {
      showToast("Clasificador no inicializado o contexto no v??lido.");
      return;
    }
    Bitmap bitmap =
        textureView.getBitmap(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
    String textToShow = classifier.classifyFrame(bitmap);
    bitmap.recycle();
    showToast(textToShow);
  }

  /** Compara dos {@code Size}s bas??ndose en sus ??reas.*/
  private static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // Echamos aqu?? para asegurar que las multiplicaciones no se desbordan
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }

  /** Muestra un cuadro de di??logo de mensaje de error. */
  public static class ErrorDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ErrorDialog newInstance(String message) {
      ErrorDialog dialog = new ErrorDialog();
      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setPositiveButton(
              android.R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  activity.finish();
                }
              })
          .create();
    }
  }
}
