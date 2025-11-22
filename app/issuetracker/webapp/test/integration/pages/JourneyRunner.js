sap.ui.define([
    "sap/fe/test/JourneyRunner",
	"customer/autoescalateops/issuetracker/test/integration/pages/IssuesList",
	"customer/autoescalateops/issuetracker/test/integration/pages/IssuesObjectPage"
], function (JourneyRunner, IssuesList, IssuesObjectPage) {
    'use strict';

    var runner = new JourneyRunner({
        launchUrl: sap.ui.require.toUrl('customer/autoescalateops/issuetracker') + '/test/flpSandbox.html#customerautoescalateopsissuetr-tile',
        pages: {
			onTheIssuesList: IssuesList,
			onTheIssuesObjectPage: IssuesObjectPage
        },
        async: true
    });

    return runner;
});

