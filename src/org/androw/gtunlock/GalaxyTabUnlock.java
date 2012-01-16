package org.androw.gtunlock;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Environment;
import android.widget.Button;
import android.view.View;
import android.os.Build;
import android.widget.TextView;
import android.provider.Settings.Secure;

import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.android.vending.licensing.AESObfuscator;

import org.androw.gtunlock.UnlockHandler;

public class GalaxyTabUnlock extends Activity
{
	private UnlockHandler handler = null;
	private String model;
	
	
	private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    
    private static final byte[] SALT = new byte[] {
        56, 89, 16,  66, 102, 102, 112, 67,  58,  19, 116, 102,   1, 27, 99, 24, 75,   4, 7, 96 
    };
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhSqOx7GMvJ7kNFMWz1DhLGwnrqznTR52ivRDiVy89904K6c1P8K5jHpXFH5QYhAsEhYMe7QJonRlmNUI1Y6bDn9SiJ7rMR8eQos/CUd8q8qL6VTkOR9TXz27BWLUUqmkG9HmG2W90IDI6bq31brpYQeglIvYKe9jYZM1PAPWx/8JU6fqiEhFEJrG1A3o6ntLBTkn4bpDg/hheUJYf+wxu0ftpm0SkL6xykjWFI1DWR3yjxUm8CqzDUHA7EvLdav8CHgS8Kkvj055tCNjOGNC2u4/XT/fRsz+I/QD422Q330VpbSbul75pdV8PCQdRXJ7hyQL/ZiedgTlWKZtXNpJ5wIDAQAB";

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler = new UnlockHandler (this);
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		mChecker = new LicenseChecker(
            this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
            BASE64_PUBLIC_KEY
            );
		
		doCheck();


        model = Build.MODEL;
		final Button unlock = (Button) findViewById(R.id.unlock);
        unlock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				if ((model == "GT-P1000") || (model == "SGH-T849") || (model == "GT-P1000M") || (model == "SGH-I987")) {
					unlockGTP1000();
				} else if ((model == "GT-I9000") ||(model == "GT-I9000M") || (model == "SGH-I897") || (model == "SGH-T959")) {
					unlockSGS();
				} else {
					displayMessage("Phone model not recognized.");
				}
			}
		});
		final Button lock = (Button) findViewById(R.id.lock);
        lock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				if ((model == "GT-P1000") || (model == "SGH-T849") || (model == "GT-P1000M") || (model == "SGH-I987")) {
					unlockGTP1000();
				} else if ((model == "GT-I9000") ||(model == "GT-I9000M") || (model == "SGH-I897") || (model == "SGH-T959")) {
					unlockSGS();
				} else {
					displayMessage("Phone model not recognized.");
				}
			}
		});
		final Button sgtunlock = (Button) findViewById(R.id.sgtunlock);
        unlock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				unlockGTP1000();
			}
		});
		final Button sgtlock = (Button) findViewById(R.id.sgtlock);
        lock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				lockGTP1000();
			}
		});
		
		final Button sgsunlock = (Button) findViewById(R.id.sgsunlock);
        unlock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				unlockSGS();
			}
		});
		final Button sgslock = (Button) findViewById(R.id.sgslock);
        lock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				lockSGS();
			}
		});
		TextView modelView = (TextView) findViewById(R.id.model);
		modelView.setText(model);
    }
    
    private void doCheck() {
        mChecker.checkAccess(mLicenseCheckerCallback);
    }
	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow() {
            if (isFinishing()) {
                return;
            }
        }
        public void dontAllow() {
            if (isFinishing()) {
                return;
            }
            exitMessage(getString(R.string.dont_allow));
        }
        public void applicationError(ApplicationErrorCode errorCode) {
            if (isFinishing()) {
                return;
            }
            String result = String.format(getString(R.string.application_error), errorCode);
            exitMessage(result);
        }
        
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChecker.onDestroy();
    }
    public File saveNedit (String path) {
		Process p;
		DataOutputStream os;
		File sdDir = Environment.getExternalStorageDirectory ();
		File orig = new File (path);
		if (!orig.exists()) {
			return null;
		}
		File edit = new File(sdDir, orig.getName()+".modi");
		File save = new File(sdDir, orig.getName()+".orig");
		if (edit.exists() || save.exists()) {
			return null;
		}
		try {
			p = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("cat "+ path +" >> "+ edit.getAbsolutePath() +"\n");
			os.writeBytes("cat "+ path +" >> "+ save.getAbsolutePath() +"\n");
			os.writeBytes("exit\n");
			os.flush();
			try {
				p.waitFor();
				if (p.exitValue() == 255) {
					return null;
				}
			} catch (InterruptedException e) { 
				return null;
			}
		} catch (IOException e) {
			return null;
		}
		return edit;
	}
	public boolean restore (File file, String path) {
		Process p;
		DataOutputStream os;
		if (!file.exists()) {
			return false;
		}
		try {
			p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("rm "+path+"\n");
            os.writeBytes("cat "+ file.getAbsolutePath() +" >> "+path+"\n");
            os.writeBytes("rm "+ file.getAbsolutePath() +"\n");
            os.writeBytes("exit\n");
            os.flush();
            try {
				p.waitFor();
				if (p.exitValue() == 255) {
                    return false;
				}
            } catch (InterruptedException e) {
                return false;
			}  
		} catch (IOException e) {
            return false;
        }
        return true;
	}
	public boolean execute (String cmd) {
		Process p;
		DataOutputStream os;
		try {
			p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(cmd+"\n");
            os.writeBytes("exit\n");
            os.flush();
            try {
				p.waitFor();
				if (p.exitValue() == 255) {
                    return false;
				}
            } catch (InterruptedException e) {
                return false;
			}  
		} catch (IOException e) {
            return false;
        }
        return true;
	}
	public boolean remove (String path) {
		Process p;
		DataOutputStream os;
		try {
			p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("rm "+ path +"\n");
            os.writeBytes("exit\n");
            os.flush();
            try {
				p.waitFor();
				if (p.exitValue() == 255) {
                    return false;
				}
            } catch (InterruptedException e) {
                return false;
			}  
		} catch (IOException e) {
            return false;
        }
        return true;
    }	
    public void requestReboot() {
		handler.sendEmptyMessage(UnlockHandler.MSG_REBOOT);
	}
	public void requestQuit() {
		handler.sendEmptyMessage(UnlockHandler.MSG_QUIT);
	}
	public void reboot() {
		Process p;
		DataOutputStream os;
		try {
			p = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("reboot\n");
			os.writeBytes("exit\n");
			os.flush();
			try {
				p.waitFor();
				if (p.exitValue() == 255) {
					exitMessage("ERROR: It seems you are not root. Your phone can't be reboot. Reboot by hand please.");
                    return;
				}
            } catch (InterruptedException e) {
				exitMessage("ERROR: It seems you are not root. Your phone can't be reboot. Reboot by hand please.");
                return;
			} 
		}catch (IOException e) {
			exitMessage("ERROR: It seems you are not root. Your phone can't be reboot. Reboot by hand please.");
            return;
        }
	}
	public void quit() {
		GalaxyTabUnlock.this.finish();
	}
	public void exitMessage(String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(GalaxyTabUnlock.this);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				requestQuit();
			}
        });
        builder.setMessage(text);
        builder.create().show();
	}
	public void rebootMessage(String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(GalaxyTabUnlock.this);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				requestReboot();
			}
        });
        builder.setMessage(text);
        builder.create().show();
	}
	public void displayMessage(String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(GalaxyTabUnlock.this);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
        });
        builder.setMessage(text);
        builder.create().show();
	}
	public void unlockGTP1000() {
		Process p;
		DataOutputStream os;
		File sdDir = Environment.getExternalStorageDirectory ();
		
		File file = saveNedit("/efs/nv_data.bin");
		
		if (file == null) {exitMessage("ERROR: Can't backup file. Maybe you already use this tools, or you deny root requests."); return;}
		
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream input = new FileInputStream(file);
			input.read(b);
		} catch (IOException e) {
            exitMessage("ERROR: Can't read nv_data.bin.modi. Please remove file in sdcard and retry.");
			return;
		}
		
		b[1578089] = 0x00;
		b[1578254] = 0x00;
		
		try {
			FileOutputStream output = new FileOutputStream(file);
			output.write(b);
			output.flush();
			output.close();
		} catch (IOException e) {
            exitMessage("ERROR: Can't write new file. Please remove file in sdcard and retry.");
			return;
		}	
		boolean writed = restore(file, "/efs/nv_data.bin");	
		if (!writed) {exitMessage("ERROR: Can't write new file. Maybe you deny root requests. Try to reboot your phone, then use LOCK function if you are not unlocked to restore."); return;}
        boolean removedmd5 = remove ("/efs/nv_data.bin.md5");
        if (!removedmd5) {exitMessage("ERROR: Can't remove md5. Maybe you deny root requests. Else, reboot your phone and you should be unlocked."); return;}
        boolean perm = execute("chown radio.radio /efs/nv_data.bin") && execute("chmod 755 /efs/nv_data.bin");
        if (!perm) {exitMessage("ERROR: Can't write new permissions. Maybe you deny root requests. Else, reboot your phone and you should be unlocked."); return;}
        rebootMessage("Phone unlocked.");
    }
	public void lockGTP1000() {
		Process p;
		DataOutputStream os;
		File sdDir = Environment.getExternalStorageDirectory ();
		File file = new File(sdDir, "nv_data.bin.orig");
		boolean restored = restore (file, "/efs/nv_data.bin");
		if (!restored) {exitMessage("ERROR: Can't restore backup. Maybe you didn't use this tool to unlock, or you deny root requests."); return;}
		boolean removedmd5 = remove ("/efs/nv_data.bin.md5");
		if (!removedmd5) {exitMessage("ERROR: Can't remove md5. Maybe you deny root requests. Else, reboot your phone and you should be restored."); return;}
        rebootMessage("Original state restored.");
	}
	public void unlockSGS() {
		Process p;
		DataOutputStream os;
		File sdDir = Environment.getExternalStorageDirectory ();
		
		File file = saveNedit("/efs/nv_data.bin");
		
		if (file == null) {exitMessage("ERROR: Can't backup file. Maybe you already use this tools, or you deny root requests."); return;}
		
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream input = new FileInputStream(file);
			input.read(b);
		} catch (IOException e) {
            exitMessage("ERROR: Can't read nv_data.bin.modi. Please remove file in sdcard and retry.");
			return;
		}
		
		b[1578089] = 0x00;
		b[1578090] = 0x00;
		b[1578091] = 0x00;
		b[1578092] = 0x00;
		b[1578093] = 0x00;
		
		try {
			FileOutputStream output = new FileOutputStream(file);
			output.write(b);
			output.flush();
			output.close();
		} catch (IOException e) {
            exitMessage("ERROR: Can't write new file. Please remove file in sdcard and retry.");
			return;
		}	
		boolean writed = restore(file, "/efs/nv_data.bin");	
		if (!writed) {exitMessage("ERROR: Can't write new file. Maybe you deny root requests. Try to reboot your phone, then use LOCK function if you are not unlocked to restore."); return;}
        boolean removedmd5 = remove ("/efs/nv_data.bin.md5");
        if (!removedmd5) {exitMessage("ERROR: Can't remove md5. Maybe you deny root requests. Else, reboot your phone and you should be unlocked."); return;}
        rebootMessage("Phone unlocked.");
    }
	public void lockSGS() {
		Process p;
		DataOutputStream os;
		File sdDir = Environment.getExternalStorageDirectory ();
		File file = new File(sdDir, "nv_data.bin.orig");
		boolean restored = restore (file, "/efs/nv_data.bin");
		if (!restored) {exitMessage("ERROR: Can't restore backup. Maybe you didn't use this tool to unlock, or you deny root requests."); return;}
		boolean removedmd5 = remove ("/efs/nv_data.bin.md5");
		if (!removedmd5) {exitMessage("ERROR: Can't remove md5. Maybe you deny root requests. Else, reboot your phone and you should be restored."); return;}
        rebootMessage("Original state restored.");
	}
}


