sap.ui.define(['sap/fe/test/ListReport'], function(ListReport) {
    'use strict';

    var CustomPageDefinitions = {
        actions: {},
        assertions: {}
    };

    return new ListReport(
        {
            appId: 'customer.autoescalateops.issuetracker',
            componentId: 'IssuesList',
            contextPath: '/Issues'
        },
        CustomPageDefinitions
    );
});