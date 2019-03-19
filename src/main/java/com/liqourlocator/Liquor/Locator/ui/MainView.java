package com.liqourlocator.Liquor.Locator.ui;

import com.liqourlocator.Liquor.Locator.model.Establishment;
import com.liqourlocator.Liquor.Locator.model.EstablishmentType;
import com.liqourlocator.Liquor.Locator.repository.EstablishmentRepository;
import com.liqourlocator.Liquor.Locator.repository.EstablishmentTypeRepository;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addons.searchbox.SearchBox;

import javax.servlet.annotation.WebServlet;
import java.util.List;

@Theme("valo")
@Widgetset(value = "com.vaadin.tapio.googlemaps.demo.DemoWidgetset")
@SpringUI(path = "/")
public class MainView extends UI
{
    @Autowired
    private EstablishmentRepository establishmentRepository;
    @Autowired
    private EstablishmentTypeRepository establishmentTypeRepository;


    private ComboBox<EstablishmentType> typesComboBox;
    private String apiKey = "AIzaSyA0HS0GBYbxEbdXhzsP7sUnk2MDT7j3XFw";
    private List<Establishment> establishments;
    private VerticalLayout panelLayout;
    private GoogleMap googleMap;

    @Override
    protected void init(VaadinRequest vaadinRequest)
    {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        SearchBox searchBox = new SearchBox("Search", SearchBox.ButtonPosition.LEFT);
        searchBox.addSearchListener(searchEvent ->
                {
                    if (typesComboBox.isEmpty())
                    {
                        establishments = establishmentRepository.findEstablishmentsByTown(searchEvent.getSearchTerm());
                    }
                    else
                    {
                        establishments = establishmentRepository.findEstablishmentsByTownAndType(searchEvent.getSearchTerm(),
                                typesComboBox.getSelectedItem().get().getType());
                    }
                    displayEstablishments(establishments);
                }
        );
        HorizontalLayout mapLayout = new HorizontalLayout();
        mapLayout.setSizeFull();

        googleMap = new GoogleMap(apiKey, null, "english");
        googleMap.setSizeFull();
        googleMap.setZoom(7);
        googleMap.setCenter(new LatLon(44.6488, -63.5752));

        panelLayout = new VerticalLayout();

        Panel closeEstablishmentsPanel = new Panel("Found Establishments");
        closeEstablishmentsPanel.setStyleName(ValoTheme.PANEL_WELL);
        closeEstablishmentsPanel.setContent(panelLayout);
        closeEstablishmentsPanel.setSizeFull();

        mapLayout.addComponents(googleMap, closeEstablishmentsPanel);
        mapLayout.setExpandRatio(googleMap, 1.0f);
        mapLayout.setExpandRatio(closeEstablishmentsPanel, 0.2f);

        typesComboBox = new ComboBox<>("License Type: ");
        List<EstablishmentType> types = establishmentTypeRepository.findAllEstablishmentTypes();
        System.out.println("size: " + types.size());
        typesComboBox.setItems(types);

        mainLayout.addComponents(searchBox, typesComboBox, mapLayout);
        mainLayout.setComponentAlignment(searchBox, Alignment.TOP_LEFT);

        mainLayout.setExpandRatio(mapLayout, 1.0f);

        setContent(mainLayout);
    }


    private void displayEstablishments(List<Establishment> establishments)
    {
        panelLayout.removeAllComponents();
        googleMap.clearMarkers();

        if (establishments.size() == 0)
        {
            Label noneFound = new Label("No Establishments Found");
            panelLayout.addComponent(noneFound);
            return;
        }

        //latlng could be null, so cycle through and find the first that isn't null and set the camera to it
        for (int i = 0; i < establishments.size(); i++)
        {
            if (establishments.get(i).getLatLong() != null)
            {
                googleMap.setCenter(establishments.get(i).getLatLong());
                googleMap.setZoom(15);
                break;
            }
        }

        establishments.forEach(establishment ->
        {
            Label esta = new Label(establishment.getEstablishment());
            panelLayout.addComponent(esta);

            LatLon establishmentLocation = establishment.getLatLong();
            if (establishmentLocation != null)
            {
                googleMap.addMarker(establishment.getEstablishment(), establishment.getLatLong(), false, null);
            }
        });
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MainView.class, productionMode = false, widgetset = "com.vaadin.tapio.googlemaps.demo.DemoWidgetset")
    public static class MyUIServlet extends VaadinServlet
    {

    }
}