package com.fathzer.jchess.swing.widget;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;

public class JComboBoxWithDisabledItems<E> extends JComboBox<E> {
	private static final long serialVersionUID = 1L;

    private transient ListCellRenderer<? super E> baseRenderer;

    public JComboBoxWithDisabledItems() {
        super(new DisabledItemComboBoxModel<>());

        super.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final Component component = baseRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!getCastedModel().isEnabled.test(value)) {
                component.setEnabled(false);
            }
            return component;
        });
    }

    private DisabledItemComboBoxModel<E> getCastedModel() {
    	return (DisabledItemComboBoxModel<E>)getModel();
    }

    @Override
    public void setRenderer(ListCellRenderer<? super E> renderer) {
        this.baseRenderer = renderer;
    }

    public void setEnabledItems(Predicate<E> isEnabled) {
        getCastedModel().isEnabled = isEnabled;
    }

    public void setAllowDisabledItemSelection(boolean allowDisabledItemSelection) {
        getCastedModel().setAllowDisabledItemSelection(allowDisabledItemSelection);
    }


    private static final class DisabledItemComboBoxModel<E> extends DefaultComboBoxModel<E> {
		private static final long serialVersionUID = 1L;

		private transient Predicate<E> isEnabled;
        private boolean allowDisabledItemSelection;
        
        private DisabledItemComboBoxModel() {
            isEnabled = i->true;
            allowDisabledItemSelection = false;
        }

        @SuppressWarnings("unchecked")
		@Override
        public void setSelectedItem(Object anObject) {
            if (allowDisabledItemSelection || isEnabled.test((E) anObject)) {
                super.setSelectedItem(anObject);
            }
        }

        private void setAllowDisabledItemSelection(boolean allowDisabledItemSelection) {
            this.allowDisabledItemSelection = allowDisabledItemSelection;
        }
    }
}
