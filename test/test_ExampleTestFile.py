"""An example file for writing unit tests with the unittest framework
in python. All test functions must be prefixed with "test_" for the
command that runs all the tests to find it."""

import unittest

import ExampleClassFile


class TestExampleClass(unittest.TestCase):
    """Organise your tests inside a class like this, prefixed with 'test_'."""

    def setUp(self):
        """Use this set up function to initialise anything you
        may want available to all your test functions below."""
        self.example_member = None

    def test_example_function(self):
        """Test functions must be prefixed with "test_".
        Test the ExampleMethod() found in the ExampleClass that
        we are testing. It should just return True."""
        example_object = ExampleClassFile.ExampleClass()
        self.assertEqual(example_object.example_method(), True)

    def test_another_example_function(self):
        self.assertTrue(True)
