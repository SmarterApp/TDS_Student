package AIR.ResourceBundler.Xml;

import java.io.File;

import AIR.Common.Web.Session.Server;

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
