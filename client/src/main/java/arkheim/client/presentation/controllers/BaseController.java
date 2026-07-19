package arkheim.client.presentation.controllers;

import arkheim.client.presentation.navigation.Navigator;

public class BaseController {
    protected Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }
}
