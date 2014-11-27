package org.ramo.klevis.p2.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;

public class Utils {

	
	public static List<IInstallableUnit> toList(IQueryResult<IInstallableUnit> query) {
		List<IInstallableUnit> list = new ArrayList<IInstallableUnit>();
		for (IInstallableUnit iInstallableUnit : query) {

			System.out.println(iInstallableUnit);
			list.add(iInstallableUnit);

		}
		return list;
	}	
}
