import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:vector_math/vector_math_64.dart' as vm;

void main() => runApp(CarvFlutterUi());

class CarvFlutterUi extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'UI components',
      initialRoute: "/",
      routes: {
        '/': (context) => Text("No active view")
      },
    );
  }
}