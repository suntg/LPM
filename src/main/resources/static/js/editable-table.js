var EditableTable = function () {
    // let baseUrl = "http://192.168.100.119:8080";
    return {

        //main function to initiate the module
        init: function () {
            function restoreRow(oTable, nRow) {
                var aData = oTable.fnGetData(nRow);
                var jqTds = $('>td', nRow);

                for (var i = 0, iLen = jqTds.length; i < iLen; i++) {
                    oTable.fnUpdate(aData[i], nRow, i, false);
                }

                oTable.fnDraw();
            }

            function editRow(oTable, nRow) {

                var aData = oTable.fnGetData(nRow);
                var jqTds = $('>td', nRow);
                jqTds[0].innerHTML = '<input type="text" class="form-control small" value="' + aData[0] + '">';
                jqTds[1].innerHTML = '<input type="text" class="form-control small" value="' + aData[1] + '">';
                // jqTds[2].innerHTML = '<input type="text" class="form-control small" value="' + aData[2] + '">';
                // jqTds[3].innerHTML = '<input type="text" class="form-control small" value="' + aData[3] + '">';
                jqTds[2].innerHTML = '<input type="text" class="form-control small" value="' + aData[2] + '">';
                jqTds[3].innerHTML = '<input type="text" class="form-control small" value="' + aData[3] + '">';
                jqTds[4].innerHTML = '<input type="text" class="form-control small" value="' + aData[4] + '">';
                jqTds[5].innerHTML = '<input type="text" class="form-control small" value="' + aData[5] + '">';
                jqTds[6].innerHTML = '<input type="text" class="form-control small" value="' + aData[6] + '">';
                jqTds[7].innerHTML = '<input type="text" class="form-control small" value="' + aData[7] + '">';
                jqTds[8].innerHTML = '<input type="text" class="form-control small" value="' + aData[8] + '">';
                jqTds[9].innerHTML = '<input type="text" class="form-control small" value="' + aData[9] + '">';
                jqTds[10].innerHTML = '<input type="text" class="form-control small" value="' + aData[10] + '">';
                jqTds[11].innerHTML = '<input type="text" class="form-control small" value="' + aData[11] + '">';
                jqTds[12].innerHTML = '<input type="text" class="form-control small" value="' + aData[12] + '">';
                jqTds[13].innerHTML = '<input type="text" class="form-control small" value="' + aData[13] + '">';
                jqTds[14].innerHTML = '<input type="text" class="form-control small" value="' + aData[14] + '">';
                jqTds[15].innerHTML = '<input type="text" class="form-control small" value="' + aData[15] + '">';
                jqTds[16].innerHTML = '<input type="text" class="form-control small" value="' + aData[16] + '">';
                jqTds[17].innerHTML = '<input type="text" class="form-control small" value="' + aData[17] + '">';
                jqTds[18].innerHTML = '<input type="text" class="form-control small" value="' + aData[18] + '">';
                jqTds[19].innerHTML = '<input type="text" class="form-control small" value="' + aData[19] + '">';
                jqTds[20].innerHTML = '<input type="text" class="form-control small" value="' + aData[20] + '">';
                jqTds[21].innerHTML = '<input type="text" class="form-control small" value="' + aData[21] + '">';
                jqTds[22].innerHTML = '<input type="text" class="form-control small" value="' + aData[22] + '">';
                jqTds[23].innerHTML = '<a class="edit" href="">Save</a>';
                jqTds[24].innerHTML = '<a class="cancel" href="">Cancel</a>';
            }

            function saveRow(oTable, nRow) {
                var jqInputs = $('input', nRow);
                oTable.fnUpdate(jqInputs[0].value, nRow, 0, false);
                oTable.fnUpdate(jqInputs[1].value, nRow, 1, false);
                // oTable.fnUpdate(jqInputs[2].value, nRow, 2, false);
                // oTable.fnUpdate(jqInputs[3].value, nRow, 3, false);
                oTable.fnUpdate(jqInputs[2].value, nRow, 2, false);
                oTable.fnUpdate(jqInputs[3].value, nRow, 3, false);
                oTable.fnUpdate(jqInputs[4].value, nRow, 4, false);
                oTable.fnUpdate(jqInputs[5].value, nRow, 5, false);
                oTable.fnUpdate(jqInputs[6].value, nRow, 6, false);
                oTable.fnUpdate(jqInputs[7].value, nRow, 7, false);
                oTable.fnUpdate(jqInputs[8].value, nRow, 8, false);
                oTable.fnUpdate(jqInputs[9].value, nRow, 9, false);
                oTable.fnUpdate(jqInputs[10].value, nRow, 10, false);
                oTable.fnUpdate(jqInputs[11].value, nRow, 11, false);
                oTable.fnUpdate(jqInputs[12].value, nRow, 12, false);
                oTable.fnUpdate(jqInputs[13].value, nRow, 13, false);
                oTable.fnUpdate(jqInputs[14].value, nRow, 14, false);
                oTable.fnUpdate(jqInputs[15].value, nRow, 15, false);
                oTable.fnUpdate(jqInputs[16].value, nRow, 16, false);
                oTable.fnUpdate(jqInputs[17].value, nRow, 17, false);
                oTable.fnUpdate(jqInputs[18].value, nRow, 18, false);
                oTable.fnUpdate(jqInputs[19].value, nRow, 19, false);
                oTable.fnUpdate(jqInputs[20].value, nRow, 20, false);
                oTable.fnUpdate(jqInputs[21].value, nRow, 21, false);
                oTable.fnUpdate(jqInputs[22].value, nRow, 22, false);

                var strategy = {};
                strategy.id = jqInputs[0].value;
                strategy.currencyPair = jqInputs[1].value;
                // strategy.transactionMultiple = jqInputs[2].value;
                // strategy.transactionRate = jqInputs[3].value;
                strategy.stopProfitMoney = jqInputs[2].value;
                strategy.overallProfitStopRatio = jqInputs[3].value;
                strategy.traceFlag = jqInputs[4].value;
                strategy.stopPriceRatio = jqInputs[5].value;
                strategy.reseauStopProfitFlag = jqInputs[6].value;
                strategy.reseauProfitStopRatio = jqInputs[7].value;
                strategy.reseauFallbackRatio = jqInputs[8].value;
                strategy.tracePurchaseRatio = jqInputs[9].value;
                strategy.averagePriceStrategy = jqInputs[10].value;
                strategy.monitorFlag = jqInputs[11].value;
                strategy.state = jqInputs[12].value;
                strategy.strategySequence = jqInputs[13].value;
                strategy.step = jqInputs[14].value;
                strategy.positionNumber = jqInputs[15].value;
                strategy.totalPositionNumber = jqInputs[16].value;
                strategy.klineInterval = jqInputs[17].value;
                // strategy.largeAmount = jqInputs[18].value;
                strategy.topAmount = jqInputs[18].value;
                strategy.bottomAmount = jqInputs[19].value;
                strategy.tradingMethodFlag = jqInputs[20].value;
                strategy.unlimitedPositionFlag = jqInputs[21].value;
                strategy.budget = jqInputs[22].value;
                $.ajax({
                    type: "post",
                    url: baseUrl+"/transactionStrategory/saveOrUpdateStrategy",
                    data: JSON.stringify(strategy),
                    datatype: "json",
                    contentType: "application/json;charset=utf-8",
                    success: function (retMsg) {
                        // if (retMsg.code == 200) {
                        //     alert("success");
                        // } else {
                        //     alert("false");
                        // }
                    }
                })

                oTable.fnUpdate('<a class="edit" href="">Edit</a>', nRow, 23, false);
                oTable.fnUpdate('<a class="delete" href="">Delete</a>', nRow, 24, false);
                oTable.fnDraw();
            }

            function cancelEditRow(oTable, nRow) {
                var jqInputs = $('input', nRow);
                oTable.fnUpdate(jqInputs[0].value, nRow, 0, false);
                oTable.fnUpdate(jqInputs[1].value, nRow, 1, false);
                oTable.fnUpdate(jqInputs[2].value, nRow, 2, false);
                oTable.fnUpdate(jqInputs[3].value, nRow, 3, false);
                oTable.fnUpdate('<a class="edit" href="">Edit</a>', nRow, 4, false);
                oTable.fnDraw();
            }

            var oTable = $('#editable-sample').dataTable({
                "aLengthMenu": [
                    [5, 15, 20, -1],
                    [5, 15, 20, "All"] // change per page values here
                ],
                // set the initial value
                "iDisplayLength": 5,
                "sDom": "<'row'<'col-lg-6'l><'col-lg-6'f>r>t<'row'<'col-lg-6'i><'col-lg-6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sLengthMenu": "_MENU_ records per page",
                    "oPaginate": {
                        "sPrevious": "Prev",
                        "sNext": "Next"
                    }
                },
                "aoColumnDefs": [{
                    'bSortable': false,
                    'aTargets': [0]
                }
                ]
            });

            jQuery('#editable-sample_wrapper .dataTables_filter input').addClass("form-control medium"); // modify table search input
            jQuery('#editable-sample_wrapper .dataTables_length select').addClass("form-control xsmall"); // modify table per page dropdown

            var nEditing = null;

            $('#editable-sample_new').click(function (e) {
                e.preventDefault();
                var aiNew = oTable.fnAddData(['', '', '', '',
                    '<a class="edit" href="">Edit</a>', '<a class="cancel" data-mode="new" href="">Cancel</a>'
                ]);
                var nRow = oTable.fnGetNodes(aiNew[0]);
                editRow(oTable, nRow);
                nEditing = nRow;
            });

            $('#editable-sample a.delete').live('click', function (e) {
                e.preventDefault();

                if (confirm("Are you sure to delete this row ?") == false) {
                    return;
                }

                var nRow = $(this).parents('tr')[0];
                oTable.fnDeleteRow(nRow);
                alert("Deleted! Do not forget to do some ajax to sync with backend :)");
            });

            $('#editable-sample a.cancel').live('click', function (e) {
                e.preventDefault();
                if ($(this).attr("data-mode") == "new") {
                    var nRow = $(this).parents('tr')[0];
                    oTable.fnDeleteRow(nRow);
                } else {
                    restoreRow(oTable, nEditing);
                    nEditing = null;
                }
            });

            $('#editable-sample a.edit').live('click', function (e) {
                e.preventDefault();

                /* Get the row as a parent of the link that was clicked on */
                var nRow = $(this).parents('tr')[0];

                if (nEditing !== null && nEditing != nRow) {
                    /* Currently editing - but not this row - restore the old before continuing to edit mode */
                    restoreRow(oTable, nEditing);
                    editRow(oTable, nRow);
                    nEditing = nRow;
                } else if (nEditing == nRow && this.innerHTML == "Save") {
                    /* Editing this row and want to save it */
                    saveRow(oTable, nEditing);
                    nEditing = null;
                    // alert("Updated! Do not forget to do some ajax to sync with backend :)");
                } else {
                    /* No edit in progress - let's start one */
                    editRow(oTable, nRow);
                    nEditing = nRow;
                }
            });

        }

    };

}();