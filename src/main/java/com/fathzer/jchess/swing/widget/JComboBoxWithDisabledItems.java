package com.fathzer.jchess.swing.widget;
import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class JComboBoxWithDisabledItems<E> extends JComboBox<E> {
	private static final long serialVersionUID = 1L;

    private transient ListCellRenderer<? super E> baseRenderer;

    public JComboBoxWithDisabledItems() {
        super(new DisabledItemComboBoxModel<>());

        super.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final Component component = baseRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (getCastedModel().disabledItems.contains(value)) {
                if (isSelected) {
                    component.setBackground(UIManager.getColor("ComboBox.background"));
                }
                component.setForeground(UIManager.getColor("Label.disabledForeground"));
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

    public void setDisabledItems(Set<E> disabledItems) {
        getCastedModel().setDisabledItems(disabledItems);
    }

    public void setAllowDisabledItemSelection(boolean allowDisabledItemSelection) {
        getCastedModel().setAllowDisabledItemSelection(allowDisabledItemSelection);
    }


    private static final class DisabledItemComboBoxModel<E> extends DefaultComboBoxModel<E> {
		private static final long serialVersionUID = 1L;

		private final transient Set<E> disabledItems;
        private boolean allowDisabledItemSelection;
        
        private DisabledItemComboBoxModel() {
            disabledItems = new HashSet<>();
            allowDisabledItemSelection = false;
        }

        @Override
        public void setSelectedItem(Object anObject) {
            if (allowDisabledItemSelection || !disabledItems.contains(anObject)) {
                super.setSelectedItem(anObject);
            }
        }

        private void setDisabledItems(Set<E> disabledItems) {
            this.disabledItems.clear();
            this.disabledItems.addAll(disabledItems);
        }

        private void setAllowDisabledItemSelection(boolean allowDisabledItemSelection) {
            this.allowDisabledItemSelection = allowDisabledItemSelection;
        }
    }
}
