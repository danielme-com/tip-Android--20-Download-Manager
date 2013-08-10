package com.danielme.tipsandroid.downloadmanager;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Toast;

import com.danieme.tipsandroid.downloadmanager.R;


/**
 * 
 * @author danielme.com
 *
 */
public class MainActivity extends Activity
{
	//id de la descarga solicitada (tener en cuenta que el servicio puede gestionar múltiples descargas simultáneas)
	private long id;
	
	private DownloadManager downloadManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		//registramos el receiver para ser notificados del final de la descarga
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
	    registerReceiver(downloadReceiver, filter);
	}
	
	 @Override
	 protected void onResume() 
	 {	 
		super.onResume();	    
		IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(downloadReceiver, intentFilter);
	 }
	  
	 @Override
	 protected void onPause() 
	 {
		super.onPause();	    
	    unregisterReceiver(downloadReceiver);
	 }
	
	public void descargar(View button)
	{
		downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://danielmedotcom.files.wordpress.com/2012/02/icon-danielme.png?w=630"));
		//podemos limitar la descarga a un tipo de red (opcional) 
		//IMPORTANTE: esta opción la comento porque da problemas en el emulador 
		//request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		//esta información  se mostrará en el área de notificaciones
		request.setTitle("Descarga");
		request.setDescription("Prueba del servicio Download Manager.");

		//vamos a guardar el fichero (opcional). ver tip 5
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{ 
			request.setDestinationInExternalFilesDir(this, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tipsAndroid",System.currentTimeMillis() + "danielme.png");
		}
		
		//iniciamos la descarga
		id = downloadManager.enqueue(request);
		
	}
	
	//muestra las últimas descargas realizadas con el servicio
	public void ver(View button)
	{
	  Intent intent = new Intent();
	  intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
	  startActivity(intent);		
	}
	
	 private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		  
		 //gestionamos la finalización de la descarga
		  @Override
		  public void onReceive(Context context, Intent intent) 
		  {
		   DownloadManager.Query query = new DownloadManager.Query();
		   query.setFilterById(id, 0);
		   Cursor cursor = downloadManager.query(query);
		     
		   if(cursor.moveToFirst())
		   {
		    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
		    int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
		      
		    if(status == DownloadManager.STATUS_SUCCESSFUL)
		    {
		    	//podemos recuperar el fichero descargado
		   	  	ParcelFileDescriptor file = null;
				try 
				{
					file = downloadManager.openDownloadedFile(id);
					Toast.makeText(MainActivity.this,"Fichero obtenido con éxito!! ",Toast.LENGTH_LONG).show();
				} 
				catch (FileNotFoundException ex) 
				{				
					Toast.makeText(MainActivity.this,"Exception: " + ex.getMessage(),Toast.LENGTH_LONG).show();
				}		      
		     } 	   
		       
		   else if(status == DownloadManager.STATUS_FAILED)
		   {
		     Toast.makeText(MainActivity.this,"FAILED: " + reason,Toast.LENGTH_LONG).show();
		   }
		   else if(status == DownloadManager.STATUS_PAUSED)
		   {
		     Toast.makeText(MainActivity.this, "PAUSED: " + reason, Toast.LENGTH_LONG).show();
		    }
		   else if(status == DownloadManager.STATUS_PENDING)
		   {
			Toast.makeText(MainActivity.this, "PENDING: " + reason, Toast.LENGTH_LONG).show();

		    }
		   else if(status == DownloadManager.STATUS_RUNNING)
		   {
			     Toast.makeText(MainActivity.this, "RUNNING: " + reason, Toast.LENGTH_LONG).show();
		    }
		   }
		  }
		    
     };
	
}