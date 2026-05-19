import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // Wichtig für den MethodChannel
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'dart:async';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  runApp(const ShoplisteApp());
}

class ShoplisteApp extends StatelessWidget {
  const ShoplisteApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Shopliste',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Meine Einkaufsliste'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  // Kanal zur Android-Ebene
  static const platform = MethodChannel('de.jmporcher.shopliste/data');

  @override
  void initState() {
    super.initState();
    // Prüft direkt beim Start, ob Google Assistant ein Extra mitgegeben hat
    _checkAndroidExtras();
  }

  Future<void> _checkAndroidExtras() async {
    try {
      final String? itemName = await platform.invokeMethod('getSharedData');
      if (itemName != null && itemName.isNotEmpty) {
        DatabaseService().addItem(itemName);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('$itemName hinzugefügt!')),
          );
        }
      }
    } on PlatformException catch (e) {
      print("Fehler beim Empfang der Extras: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: StreamBuilder<QuerySnapshot>(
        stream: FirebaseFirestore.instance
            .collection('list')
            .orderBy('createdAt', descending: true)
            .snapshots(),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }

          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          final docs = snapshot.data?.docs ?? [];

          if (docs.isEmpty) {
            return const Center(
              child: Padding(
                padding: EdgeInsets.all(20.0),
                child: Text(
                  'Deine Liste ist leer. Sag zum Beispiel:\n"Okay Google, füge Hafermilch zu Shopliste hinzu!"',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 16, color: Colors.black54),
                ),
              ),
            );
          }

          return ListView.builder(
            itemCount: docs.length,
            padding: const EdgeInsets.all(8.0),
            itemBuilder: (context, index) {
              final data = docs[index].data() as Map<String, dynamic>;
              final String itemName = data['name'] ?? 'Unbekannt';

              return GestureDetector(
                onTap: () => docs[index].reference.delete(),
                child: Container(
                  margin: const EdgeInsets.symmetric(vertical: 6.0, horizontal: 4.0),
                  padding: const EdgeInsets.all(16.0),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(12.0),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withValues(alpha: 0.05),
                        blurRadius: 5,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Row(
                    children: [
                      const SizedBox(width: 15),
                      Expanded(
                        child: Text(
                          itemName,
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w500,
                            color: Colors.black87,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}

class DatabaseService {
  final CollectionReference _shoppingList =
  FirebaseFirestore.instance.collection('list');

  Future<void> addItem(String name) async {
    await _shoppingList.add({
      'name': name,
      'createdAt': FieldValue.serverTimestamp(),
    });
  }
}