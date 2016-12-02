/*************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at 
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package AIR.ResourceBundler.Xml;

import java.io.File;

import AIR.Common.Web.Session.Server;

/*This class provides method to format file path with client name and handles paths starting with '~'. In .net ResourcesLink, ResourcesSingleton and FileSetInput uses funtion pointer for this format methed, since Java has nothing equivalent, we put that funtion inside this methed*/
public class PathFormatter {

	private static final String _defaultClientStylePath = "AIR";

	/// <summary>
	/// Get the client style path.
	/// </summary>
	/// <remarks>
	/// StudentSettings.GetClientStylePath()
	/// </remarks>
	public static String getResourcesPathFormatArgs() {
		String str = _resourcePathFormatArgsHandler();
		str = (str != null) ? str : _defaultClientStylePath;
		return str;
	}

	private static String _resourcePathFormatArgsHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	String format(String path) {
		// check for replacement
		if (path.indexOf("%s") > 0) {
			String formatArgs = getResourcesPathFormatArgs();
			String resourcePath = String.format(path, formatArgs);

			String physicalPath = resourcePath.startsWith("~") ? Server.mapPath(resourcePath) : resourcePath;

			if (!new File(physicalPath).exists()) {
				formatArgs = _defaultClientStylePath;
				resourcePath = String.format(path, formatArgs);
			}

			path = resourcePath;
		}

		return path;
	}
}
