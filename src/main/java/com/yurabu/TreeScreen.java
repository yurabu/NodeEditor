package com.yurabu;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Container;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.yurabu.model.Node;

@SuppressWarnings("serial")
public class TreeScreen extends VerticalLayout {
	
	private static final String PERSISTENCE_UNIT="vaadindemo";
	private static final String NUMBER="number";
	private static final String ADD="add";
	private static final String REMOVE="remove";
	
	private final static Logger log = LoggerFactory.getLogger(TreeScreen.class.getName());
	
	private TreeTable ttable;
	
	private MutableLocalEntityProvider<Node> entityProvider;
	
	/**
	 * Calculates sum of numbers 
	 * by route to root
	 * 
	 * @param node
	 * @return sum
	 */
	private Long calculateSumNumber(Node node) {
		Long sum=0L;

		while ((node = node.getParent()) != null) {
			sum += node.getNumber();
		}
		
		return sum;
	}
	
	/**
	 * Add node to table
	 * Used by clicking "-" item
	 * by given current node
	 * 
	 * @param currentNode
	 */
	private void addNode(Node currentNode) {
		
		JPAContainer<Node> nodes = (JPAContainer<Node>) ttable.getContainerDataSource();
		
		Node newNode = new Node(currentNode);
		newNode.setNumber(calculateSumNumber(newNode));
		
		Object newItem = nodes.addEntity(newNode);
		
		ttable.setCollapsed(newItem, false);
		ttable.setPageLength(ttable.size());
		//ttable.refreshRowCache();
	}
	
	/**
	 * Removes node and its children
	 * by given id
	 * 
	 * @param itemId
	 */
	private void removeNode(Object itemId) {
		
		JPAContainer<Node> nodes = (JPAContainer<Node>) ttable.getContainerDataSource();
		
		for (Object childId : nodes.getChildren(itemId))
			removeNode(nodes.getItem(childId).getItemId());
				
		nodes.removeItem(itemId);

		ttable.setPageLength(ttable.size());
		//ttable.refreshRowCache();
	}
	

	/**
	 * Recalculates numbers of children
	 * and update the tree
	 * 
	 * @param number
	 * @param itemId
	 */
	private void updateNumbers(Long number, Object itemId) {
		
		JPAContainer<Node> nodes = (JPAContainer<Node>) ttable.getContainerDataSource();
		
		// update number
		entityProvider.updateEntityProperty(itemId, "number", number);
		
		for (Object childId : nodes.getChildren(itemId)) {
			Node childNode = nodes.getItem(childId).getEntity();
			Long childNumber = calculateSumNumber(childNode);
			updateNumbers(childNumber, childId);
		}

		ttable.refreshRowCache();
	}

	/**
	 * Constructor creates view of tree
	 * Tree data maps in TreeTable by JPAContainer
	 * Numbers in nodes calculates by sum of its parents up to the root,
	 * they are editable and editing numbers affects to its children.
	 * Nodes can be added and removed by clicking "+" and "-" buttons 
	 * 
	 * @see JPAContainer
	 */
	public TreeScreen() {
		setMargin(true);
		
        // factory to create entity manager
        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        
        // an entity manager to create entity provider
        EntityManager em = emf.createEntityManager();
        
        // entity provider to create a container no cache     
        entityProvider = new MutableLocalEntityProvider<Node>(Node.class, em);
        
        JPAContainer<Node> nodes = new JPAContainer<Node> (Node.class); 
        nodes.setEntityProvider(entityProvider);
        
		/*
		JPAContainer<Node> nodes = JPAContainerFactory.make(Node.class, PERSISTENCE_UNIT);
		*/
		if (nodes.size() == 0) {
			// add root
			nodes.addEntity(new Node(1L));
		}
		
        // Set it up for hierarchical representation
		nodes.setParentProperty("parent");
		
		ttable = new TreeTable("", nodes);
		
		// fake items for add remove interactions
		ttable.addGeneratedColumn(ADD, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				return "+";
			}
			
		});
		ttable.addGeneratedColumn(REMOVE, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				return "-";
			}
			
		});
		
		// set table properties
		ttable.setColumnHeaderMode(TreeTable.ColumnHeaderMode.HIDDEN);
		ttable.setVisibleColumns(NUMBER, ADD, REMOVE);
		/*
		ttable.setColumnWidth(ADD, 7);
		ttable.setColumnWidth(REMOVE, 7);
		ttable.setWidth("100%");
		*/
		ttable.setPageLength(ttable.size());
		ttable.setEditable(true);
        ttable.setImmediate(true);
        
        ttable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            private static final long serialVersionUID = 5548609446898735032L;

            public void itemClick(ItemClickEvent event) {
            	
            	if (NUMBER.equals(event.getPropertyId())) {
            		
            	} else if (ADD.equals(event.getPropertyId())) { // "+" click
            		
            		// add new entry, get current node
            		addNode(((JPAContainerItem<Node>) event.getItem()).getEntity());
            		
            	} else if (REMOVE.equals(event.getPropertyId())) { // "-" click

            		// removes items from table
            		removeNode(((JPAContainerItem<Node>) event.getItem()).getItemId());
            	}
            	
            }
        });
        
        // creating custom fild factory to handle edit number event
        // enables validation and set numbers of children
        ttable.setTableFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field<?> createField(Container container, final Object itemId,
					Object propertyId, Component uiContext) {
				
				if (NUMBER.equals(propertyId)) {
					final TextField field = new TextField((String) propertyId);
					
					// set validation
					field.setData(itemId);
					field.setNullSettingAllowed(false);
					field.addValidator(new LongRangeValidator("Value must be Long", Long.MIN_VALUE, Long.MAX_VALUE));
					field.setImmediate(true);
					field.setValidationVisible(true);
					
					// update numbers
					field.addTextChangeListener(new TextChangeListener() {
						@Override
						public void textChange(TextChangeEvent event) {
							log.info("Number changed: " + event.getText());

				            try {
				                field.validate();
				                
				                Long number = Long.parseLong(event.getText());
				                
				                updateNumbers(number, itemId);
				                
				            } catch (NumberFormatException | InvalidValueException e) {
				            	log.error("Parse number faled: " + e.getMessage());
//				                Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
				            }

						}}); 
					
					return field;
				}
				
                // Otherwise use the default field factory 
                return super.createField(container, itemId, propertyId, uiContext);
			}});

        // Expand the tree completely
        for (Object itemId: ttable.getItemIds())
            ttable.setCollapsed(itemId, false);
		
		addComponent(ttable);
	}
	
}
