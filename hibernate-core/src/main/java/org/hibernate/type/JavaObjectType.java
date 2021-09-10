/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type;

import org.hibernate.type.descriptor.java.JavaObjectTypeDescriptor;
import org.hibernate.type.descriptor.jdbc.ObjectJdbcTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class JavaObjectType extends AbstractSingleColumnStandardBasicType<Object> {
	/**
	 * Singleton access
	 */
	public static final JavaObjectType INSTANCE = new JavaObjectType();

	public JavaObjectType() {
		super( ObjectJdbcTypeDescriptor.INSTANCE, JavaObjectTypeDescriptor.INSTANCE );
	}

	@Override
	public String getName() {
		return "JAVA_OBJECT";
	}
}