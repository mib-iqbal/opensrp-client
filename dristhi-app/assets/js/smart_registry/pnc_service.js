angular.module("smartRegistry.services")
    .service('PNCService', function (SmartHelper) {
        var calculateExpectedVisitDates = function(client) {
            var delivery_date = new Date(Date.parse(client.deliveryDate));
            var visit_days = [1, 3, 7];

            var expected_visit_days = [];
            visit_days.forEach(function(offset){
                // clone the date
                var d = new Date(delivery_date.getTime());
                d.setDate(d.getDate() + offset);

                expected_visit_days.push({
                    day: offset,
                    date: d.getUTCFullYear() + '-'
                        + SmartHelper.zeroPad(d.getUTCMonth() + 1) + '-'
                        + SmartHelper.zeroPad(d.getUTCDate())
                });
            });

            return expected_visit_days;
        };

        var getFirst3Visits = function(delivery_date, services_provided) {
            /// determine the 7 day period end date
            var end_date = new Date(Date.parse(delivery_date));
            end_date.setDate(end_date.getDate() + 7);

            /// find PNC services with 7 day window
            pnc_services = services_provided.filter(function(service){
                return  service.name === "PNC" && Date.parse(service.date) <= end_date.getTime();
            });

            if(pnc_services.length > 0)
            {
                /// sort by service date ascending and slice to 3
                var first_3_services = pnc_services.sort(function(a, b){
                    return a.date < b.date?-1:1;
                }).slice(0, 3);

                // determine the day within the 7 day window that the service falls on
                first_3_services.forEach(function(service){
                    service.day = Math.floor((Date.parse(service.date) - Date.parse(delivery_date)) / 1000 / 60 / 60 / 24);
                });
                return first_3_services;
            }
            return [];
        };

        var preProcessFirst7Days = function(client, expected_visits, current_date)
        {
            var first_3_visits = getFirst3Visits(client.deliveryDate, client.services_provided);

            var delivery_date_ts = Date.parse(client.deliveryDate);
            var current_day = Math.floor((current_date.getTime() - delivery_date_ts) / 1000 / 60 / 60 / 24);

            /// create circle data and create status icons (only on expected visits days if the visit was done)
            /// based on expected visits
            var circle_datas = [], status_datas = [];
            expected_visits.forEach(function(expected_visit){
                var expected_visit_ts = Date.parse(expected_visit.date);
                var expected_visit_day = Math.floor((expected_visit_ts - delivery_date_ts) / 1000 / 60 / 60 / 24);
                var num_visits = first_3_visits.filter(function(visit){
                    return visit.day === expected_visit_day
                }).length;
                /// check whether the visit date is beyond the current date in which case its always of type expected and color grey
                if(expected_visit_day >= current_day)
                {
                    circle_datas.push({
                        day: expected_visit_day,
                        type: 'expected',
                        colored: false
                    });
                }
                /// check if its between delivery date and current date and there is no actual visit on said date
                else if(expected_visit_day < current_day)
                {
                    if(num_visits === 0)
                    {
                        circle_datas.push({
                            day: expected_visit_day,
                            type: 'expected',
                            colored: true
                        });

                        status_datas.push(
                            {
                                day: expected_visit_day,
                                status: 'missed'
                            }
                        );
                    }
                    else
                    {
                        status_datas.push(
                            {
                                day: expected_visit_day,
                                status: 'done'
                            }
                        );
                    }
                }
            });

            /// create circle data based on actual data
            first_3_visits.forEach(function(actual_visit){
                circle_datas.push({
                    day: actual_visit.day,
                    type: 'actual'
                });
            });

            client.visits.first_7_days.circles = circle_datas;
            client.visits.first_7_days.statuses = status_datas;

            /// determine the active color - start off as yellow
            client.visits.first_7_days.active_color = 'yellow';

            var valid_expected_visit_days = expected_visits
                .filter(function(d){
                    return d.day < current_day;
                })
                .map(function(d){
                    return d.day;
                });

            var valid_actual_visit_days = first_3_visits
                .map(function(d){
                    return d.day;
                });

            if(first_3_visits.length === 0 && current_day > 1)
            {
                client.visits.first_7_days.active_color = 'red';
            }
            else if(valid_actual_visit_days.filter(function(d){return valid_expected_visit_days.indexOf(d) !== -1}).length === valid_expected_visit_days.length)
            {
                client.visits.first_7_days.active_color = 'green';
            }

            // create tick data, tick only exist where there are no circles and can only exist on days 2,4,5 and 6
            var tick_days = [2,4,5,6].filter(function(d){
                return circle_datas.filter(function(circle_data){return circle_data.day === d}).length === 0;
            });

            var tick_datas = [];
            tick_days.forEach(function(tick_day){
                /// check if its in the future (TODO: perhaps have a function that determines if we are beyond current date)
                if(tick_day > current_day)
                {
                    tick_datas.push({
                        day: tick_day,
                        type: 'expected'
                    });
                }
                else
                {
                    tick_datas.push({
                        day: tick_day,
                        type: 'actual'
                    });
                }
            });
            client.visits.first_7_days.ticks = tick_datas;

            /// create line data
            var lines_datas = [];

            /// check if we have an actual line from day 1 to current day
            if(current_day > 1)
            {
                lines_datas.push({
                    start: 1,
                    end: Math.min(7, current_day),
                    type: 'actual'
                });
            }

            /// check if we have an expected line from current_day to day 7
            if(current_day < 7)
            {
                lines_datas.push({
                    start: Math.max(1, current_day),
                    end: 7,
                    type: 'expected'
                });
            }

            client.visits.first_7_days.lines = lines_datas;

            /// generate day nos - put a number wherever we have a visit and where we have an expected visit in the future
            var day_nos = circle_datas
                .filter(function(d){
                    return d.type === 'actual' || d.day > current_day;
                })
                .map(function(d){
                    return {
                        day: d.day,
                        type:d.type
                    };
                })

            client.visits.first_7_days.day_nos = day_nos;
        };

        return {
            calculateExpectedVisitDates: calculateExpectedVisitDates,
            preProcessFirst7Days: preProcessFirst7Days,
            getFirst3Visits: getFirst3Visits,
            preProcess: function (clients) {
                var current_date = new Date();
                clients.forEach(function (client) {
                        if(!client.visits)
                            client.visits = {};
                        client.visits['first_7_days'] = {};

                        // calculate expected visit data
                        var expected_visits = calculateExpectedVisitDates(client, current_date);
                        preProcessFirst7Days(client, expected_visits, current_date);
                    }
                );
            }
        }
    });