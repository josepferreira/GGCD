#!/usr/bin/python
"""BigQuery I/O PySpark example."""
from __future__ import absolute_import
import json
import pprint
import subprocess
import pyspark
from pyspark.sql import SQLContext

sc = pyspark.SparkContext()

# Use the Cloud Storage bucket for temporary BigQuery export data used
# by the InputFormat. This assumes the Cloud Storage connector for
# Hadoop is configured.
bucket = sc._jsc.hadoopConfiguration().get('fs.gs.system.bucket')
input_directory = 'gs://{}/hadoop/tmp/bigquery/pyspark_input'.format(bucket)

conf = {
    # Input Parameters.
    'mapred.bq.project.id': 'aula-ggcd',
    'mapred.bq.gcs.bucket': 'cluster-0871',
    'mapred.bq.temp.gcs.path': input_directory,
    'mapred.bq.input.project.id': 'aula-ggcd',
    'mapred.bq.input.dataset.id': 'tp',
    'mapred.bq.input.table.id': 'teste',
}

# Output Parameters.
output_dataset = 'wordcount_dataset'
output_table = 'wordcount_output'

# Load data in from BigQuery.
table_data = sc.newAPIHadoopRDD(
    'com.google.cloud.hadoop.io.bigquery.JsonTextBigQueryInputFormat',
    'org.apache.hadoop.io.LongWritable',
    'com.google.gson.JsonObject',
    conf=conf)

# Perform word count.
word_counts = (
    table_data
    .map(lambda record: json.loads(record[1]))
    .map(lambda x: (x['id'].lower(), int(x['tempo'])))
    .reduceByKey(lambda x, y: x + y))

# Display 10 results.
pprint.pprint(word_counts.take(10))

# Stage data formatted as newline-delimited JSON in Cloud Storage.
output_directory = 'gs://aula-ggcd/hadoop-nosso/pyspark_output'.format(bucket)
output_files = output_directory + '/part-*'

sql_context = SQLContext(sc)
(word_counts
 .toDF(['word', 'word_count'])
 .write.format('json').save(output_directory))

# Shell out to bq CLI to perform BigQuery import.
subprocess.check_call(
    'bq load --source_format NEWLINE_DELIMITED_JSON '
    '--replace '
    '--autodetect '
    '{dataset}.{table} {files}'.format(
        dataset=output_dataset, table=output_table, files=output_files
    ).split())

# Manually clean up the staging_directories, otherwise BigQuery
# files will remain indefinitely.
input_path = sc._jvm.org.apache.hadoop.fs.Path(input_directory)
input_path.getFileSystem(sc._jsc.hadoopConfiguration()).delete(input_path, True)
output_path = sc._jvm.org.apache.hadoop.fs.Path(output_directory)
output_path.getFileSystem(sc._jsc.hadoopConfiguration()).delete(
    output_path, True)
