package org.eclipse_icons.editor;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Property tester for commands in Editor. See plugin.xml
 * @author Jabier Martinez
 */
public class EditorPropertyTester extends PropertyTester {

	private static final String HAS_SELECTION = "hasSelection";

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equalsIgnoreCase(HAS_SELECTION)) {
			if (receiver instanceof IconsEditorPart) {
				boolean hasSelection = ((IconsEditorPart) receiver).selected;
				return hasSelection;
			}
		}
		return false;
	}

}
