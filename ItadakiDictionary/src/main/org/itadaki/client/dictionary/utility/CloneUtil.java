/*
 * Copyright (C) 2006-2007
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

/*
 * Copyright (C) 2005 Contributors of the Taka Project (see below)
 * 
 * This program is offered under the following licenses:
 *   * The "MIT style" minimal copyright license (LICENSE-TAKA)
 *   * The GNU LGPL version 2.1 or later (LICENSE-LGPL)
 *   * The GNU GPL version 2 or later (LICENSE-GPL)
 * 
 * You may, at your option, choose one of these licenses and omit the
 * alternatives. You may additionally increase the minimum offered version of
 * the GNU licenses (for instance, offer only under the GNU GPL version 3 or
 * later).
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the licenses referenced above for
 * further details.
 * 
 * History:
 *   2005-MM-DD: Matthew Francis
 *       Initial release
 */

package org.itadaki.client.dictionary.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility class providing serialisation-based deep cloning
 */
public class CloneUtil {

	/**
	 * Deep clone an object using serialisation
	 *
	 * @param object The object to clone
	 * @return The cloned object
	 */
	public static Object clone (Object object) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		Object cloneObject = null;
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream (byteArrayOutputStream);
			objectOutputStream.writeObject (object);
			byte[] objectBytes = byteArrayOutputStream.toByteArray();
			ObjectInputStream objectInputStream = new ObjectInputStream (new ByteArrayInputStream (objectBytes));
			cloneObject = objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cloneObject;

	}

}
