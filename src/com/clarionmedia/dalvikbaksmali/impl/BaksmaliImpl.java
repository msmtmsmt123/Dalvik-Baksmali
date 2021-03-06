/*
 * Copyright (C) 2013 Clarion Media, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarionmedia.dalvikbaksmali.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jf.baksmali.baksmali;
import org.jf.baksmali.dump;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Opcode;

import android.util.Log;

import com.clarionmedia.dalvikbaksmali.Baksmali;

/**
 * <p>
 * Implementation of {@link Baksmali} which calls the baksmali library.
 * </p>
 * 
 * @author Tyler Treat
 * @version 1.0 01/05/13
 * @since 1.0
 */
public class BaksmaliImpl implements Baksmali {

	boolean disassemble = true;
	boolean doDump = false;
	boolean write = false;
	boolean sort = false;
	boolean fixRegisters = false;
	boolean noParameterRegisters = false;
	boolean useLocalsDirective = false;
	boolean useSequentialLabels = false;
	boolean outputDebugInfo = true;
	boolean addCodeOffsets = false;
	boolean noAccessorComments = false;
	boolean deodex = false;
	boolean verify = false;
	boolean ignoreErrors = false;
	boolean checkPackagePrivateAccess = false;

	int apiLevel = 14;

	int registerInfo = 0;

	String dumpFileName = null;
	String outputDexFileName = null;
	String bootClassPath = null;
	StringBuffer extraBootClassPathEntries = new StringBuffer();
	List<String> bootClassPathDirs = new ArrayList<String>();
	String inlineTable = null;
	boolean jumboInstructions = false;

	public BaksmaliImpl() {
		bootClassPathDirs.add(".");
	}

	@Override
	public void decompile(String dexName, String outputDir) {
		try {
			File dexFileFile = new File(dexName);
			if (!dexFileFile.exists()) {
				Log.e(getClass().getName(), "Can't find the file " + dexName);
				return;
			}

			Opcode.updateMapsForApiLevel(apiLevel, jumboInstructions);

			// Read in and parse the dex file
			DexFile dexFile = new DexFile(dexFileFile, !fixRegisters, false);

			if (dexFile.isOdex()) {
				if (doDump) {
					Log.e(getClass().getName(), "-D cannot be used with on odex file. Ignoring -D");
				}
				if (write) {
					Log.e(getClass().getName(), "-W cannot be used with an odex file. Ignoring -W");
				}
				if (!deodex) {
					Log.e(getClass().getName(), "Warning: You are disassembling an odex file without deodexing it. You");
					Log.e(getClass().getName(), "won't be able to re-assemble the results unless you deodex it with the -x");
					Log.e(getClass().getName(), "option");
				}
			} else {
				deodex = false;

				if (bootClassPath == null) {
					bootClassPath = "core.jar:ext.jar:framework.jar:android.policy.jar:services.jar";
				}
			}

			if (disassemble) {
				String[] bootClassPathDirsArray = new String[bootClassPathDirs.size()];
				for (int i = 0; i < bootClassPathDirsArray.length; i++) {
					bootClassPathDirsArray[i] = bootClassPathDirs.get(i);
				}

				baksmali.disassembleDexFile(dexFileFile.getPath(), dexFile, deodex, outputDir, bootClassPathDirsArray, bootClassPath,
						extraBootClassPathEntries.toString(), noParameterRegisters, useLocalsDirective, useSequentialLabels,
						outputDebugInfo, addCodeOffsets, noAccessorComments, registerInfo, verify, ignoreErrors, inlineTable,
						checkPackagePrivateAccess);
			}

			if ((doDump || write) && !dexFile.isOdex()) {
				try {
					dump.dump(dexFile, dumpFileName, outputDexFileName, sort);
				} catch (IOException ex) {
					Log.e(getClass().getName(), "Error occured while writing dump file");
					ex.printStackTrace();
				}
			}
		} catch (RuntimeException ex) {
			Log.e(getClass().getName(), "\n\nUNEXPECTED TOP-LEVEL EXCEPTION:");
			ex.printStackTrace();
			return;
		} catch (Throwable ex) {
			Log.e(getClass().getName(), "\n\nUNEXPECTED TOP-LEVEL ERROR:");
			ex.printStackTrace();
			return;
		}
	}

}
