####################################
DataSet Transformations
####################################

DataSet Filters
----------------------------
No dataset filters (mapping sets to booleans) are provided yet.

DataSet Computations
-----------------------
Dataset computations convert an input dataset to an output value (other than a dataset).

.. function:: ArgMin: field

  Return datum in dataset whose field is minimized.

  :parameter: field to minimize

.. function:: ArgMax: field

  Return datum in dataset whose field is maximized.

  :parameter: field to maximize

DataSet Transformations
----------------------------

Basic Functions
***************
.. function:: Limit: n

  Return the first *n* elements of data.

  :parameter: number of elements to return

.. function:: Chain: [ t1, t2, t3 ]

  Perform a sequence of transformations on the dataset.

  :parameter: list of other *dataset transformations* to apply to dataset

Sorting
***********
Sorting operations rearrange the input data.

.. function:: SortBy: [ field1, field2, ... ]

  Sort data in ascending order.

  :parameter: one or more fields to sort by

.. function:: SortByDescending: [ field1, field2, ... ]

  Sort data in descending order.

  :parameter: one or more fields to sort by

Aggregation
***********
The aggregate operation is a very powerful way to rearrange and summarize data.

.. function:: Aggregate: { op: {}, field: x, asField: x, groupBy: [] }

  Aggregate data in a given field, optionally grouping the data by one or more other fields.

  :param op: a *set computation* operation such as ``Count``, ``Min``, ``Max``, etc. defining the aggregation function (see :ref:`set-compute` for all options)
  :param field: the field that the set computation will be applied to (may be omitted for the ``Count`` operation)
  :param asField: the field name to use for the aggregated value in the result
  :param groupBy: (optional) list of field names to group by; each distinct tuple here will provide a distinct datum in the result